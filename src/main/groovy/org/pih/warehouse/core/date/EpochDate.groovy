package org.pih.warehouse.core.date

import java.time.Instant

/**
 * Enumerates the different date epochs that are used within the app.
 *
 * An epoch is simply a date at which t=0 in the given date system. Often you will see a date represented
 * as "X time since epoch". Unfortunately not all date systems use the same epoch, hence the need for this enum.
 */
enum EpochDate {

    /**
     * The epoch for Unix time.
     *
     * This should be the default option in almost all scenarios. It is the epoch that Java uses, and so is the
     * epoch for all internal app logic.
     */
    UNIX_EPOCH(Instant.EPOCH),

    /**
     * The epoch for Excel files on Windows machines.
     *
     * Why Dec 31st, 1899 and not Jan 1st, 1900? Excel dates on Windows are 1-index based, so to get t=0,
     * we shift back a day.
     */
    EXCEL_1900(Instant.parse("1899-12-31T00:00:00Z")),

    /**
     * The epoch for Excel files on Mac machines.
     */
    EXCEL_1904(Instant.parse("1904-01-01T00:00:00Z"))

    /**
     * The Instant, ie moment in time relative to the Unix epoch, represented by this epoch date where t=0.
     * We use Instant because it is what we use for all other datetime processing within the app.
     */
    final Instant instant

    EpochDate(final Instant instant) {
        this.instant = instant
    }
}
