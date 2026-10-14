package org.pih.warehouse.regression.platform

import spock.lang.PendingFeature

import org.pih.warehouse.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class DateUtilSpec extends Specification {

    private static final String FORMAT = 'yyyy-MM-dd HH:mm:ss.SSS'

    private static Date date(String value) {
        return new SimpleDateFormat(FORMAT).parse(value)
    }

    private static String format(Date value) {
        return new SimpleDateFormat(FORMAT).format(value)
    }

    void 'the epoch date is January 1, 1970 UTC'() {
        expect:
        assert DateUtil.EPOCH_DATE.time == 0L
    }

    void 'getZoneOffset follows daylight savings for case: #scenario'() {
        expect:
        assert DateUtil.getZoneOffset(ZoneId.of(zone), Instant.parse(instant)) == ZoneOffset.of(expectedOffset)

        where:
        scenario                 | zone                | instant                || expectedOffset
        'New York in winter'     | 'America/New_York'  | '2024-01-15T12:00:00Z' || '-05:00'
        'New York in summer'     | 'America/New_York'  | '2024-07-15T12:00:00Z' || '-04:00'
        'Kigali has no DST'      | 'Africa/Kigali'     | '2024-07-15T12:00:00Z' || '+02:00'
        'a fixed offset zone'    | '+05:30'            | '2024-07-15T12:00:00Z' || '+05:30'
    }

    void 'getZoneOffset defaults to the current offset when no instant is given'() {
        expect:
        assert DateUtil.getZoneOffset(ZoneId.of('UTC')) == ZoneOffset.UTC
    }

    void 'the system zone and offset come from the JVM default timezone'() {
        given:
        Instant instant = Instant.parse('2024-07-15T12:00:00Z')

        expect:
        assert DateUtil.systemZoneId == ZoneId.systemDefault()
        assert DateUtil.getSystemZoneOffset(instant) == ZoneId.systemDefault().rules.getOffset(instant)
    }

    void 'clearTime should truncate a date to midnight without modifying the original'() {
        given:
        Date original = date('2024-03-10 17:45:30.123')

        when:
        Date cleared = DateUtil.clearTime(original)

        then:
        assert format(cleared) == '2024-03-10 00:00:00.000'
        assert format(original) == '2024-03-10 17:45:30.123'
    }

    void 'clearTime should return null when given null'() {
        expect:
        assert DateUtil.clearTime(null) == null
    }

    void 'parseDateRange should return null when given no range: "#range"'() {
        expect:
        assert DateUtil.parseDateRange(range, 'MM/dd/yyyy', ',') == null

        where:
        range << [null, '']
    }

    @PendingFeature(reason = 'parseDateRange calls a nonexistent ValidationException(String) constructor: GroovyRuntimeException')
    void 'parseDateRange should refuse a range that does not have exactly two dates: "#range"'() {
        when:
        DateUtil.parseDateRange(range, 'MM/dd/yyyy', ',')

        then:
        IllegalArgumentException e = thrown()
        e.message == 'Date range must have exactly two dates'

        where:
        range << ['01/31/2024', '01/01/2024,01/02/2024,01/03/2024']
    }

    void 'parseDateRange should parse both dates of a range using the given format and separator'() {
        when:
        Date[] range = DateUtil.parseDateRange('01/31/2024|02/29/2024', 'MM/dd/yyyy', '\\|')

        then:
        assert range.length == 2
        assert format(range[0]) == '2024-01-31 00:00:00.000'
        assert format(range[1]) == '2024-02-29 00:00:00.000'
    }

    void 'getDateRange should span the whole month at an offset of #relativeMonth months'() {
        when:
        Map range = DateUtil.getDateRange(date('2024-03-10 17:45:30.123'), relativeMonth)

        then:
        assert format(range.startDate) == expectedStart
        assert format(range.endDate) == expectedEnd

        where:
        relativeMonth || expectedStart             | expectedEnd
        0             || '2024-03-01 00:00:00.000' | '2024-03-31 23:59:59.999'
        -1            || '2024-02-01 00:00:00.000' | '2024-02-29 23:59:59.999'
        -3            || '2023-12-01 00:00:00.000' | '2023-12-31 23:59:59.999'
        1             || '2024-04-01 00:00:00.000' | '2024-04-30 23:59:59.999'
    }
}
