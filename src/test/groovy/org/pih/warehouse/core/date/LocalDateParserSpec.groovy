package org.pih.warehouse.core.date

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeParseException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.session.SessionManager

@Unroll
class LocalDateParserSpec extends Specification {

    @Shared
    LocalDateParser localDateParser

    @Shared
    SessionManager sessionManagerStub

    void setup() {
        sessionManagerStub = Stub(SessionManager)
        localDateParser = new LocalDateParser()
        localDateParser.sessionManager = sessionManagerStub
    }

    void 'parse should safely handle null dates'() {
        expect:
        assert localDateParser.parse(null) == null
    }

    void 'parse should successfully parse valid strings for case: #scenario'() {
        expect:
        assert localDateParser.parse(givenDate).toString() == expectedResult

        where:
        givenDate    || expectedResult | scenario
        "2000-01-01" || "2000-01-01"   | "Date with dashes"
        "2000 01 01" || "2000-01-01"   | "Date with spaces"
        "2000/01/01" || "2000-01-01"   | "Date with slashes"
        "20000101"   || "2000-01-01"   | "Date with no separator"
    }

    void 'parse should fail to parse invalid strings for case: #failureReason'() {
        when:
        localDateParser.parse(givenDate)

        then:
        thrown(DateTimeParseException)

        where:
        givenDate          || failureReason
        "2000-01-01T00:00" || "LocalDate should have no time component"
        "2000-01-32"       || "day out of range"
        "2000-13-01"       || "month out of range"
        "10000-13-01"      || "year out of range"
        "00-01-01"         || "two digit year format not supported"
    }

    void 'parse should successfully convert a Date to a LocalDate for case: #scenario'(){
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone(timezoneToDisplay)

        and:
        Date date = newDate(givenDate)

        when:
        LocalDate localDate = localDateParser.parse(date)

        then:
        assert localDate.toString() == expectedConvertedDate

        where:
        givenDate                | timezoneToDisplay || expectedConvertedDate | scenario
        "2000-01-01T00:00Z"      | 'Z'               || "2000-01-01"          | "UTC to UTC (no conversion)"
        "2000-01-01T00:00+05:00" | 'Z'               || "1999-12-31"          | "+05 to UTC (backwards conversion)"
        "2000-01-01T00:00-05:00" | 'Z'               || "2000-01-01"          | "-05 to UTC (forwards conversion)"
        "2000-01-01T00:00Z"      | 'GMT+05:00'       || "2000-01-01"          | "UTC to +05 (forwards conversion)"
        "2000-01-01T00:00+05:00" | 'GMT+05:00'       || "2000-01-01"          | "+05 to +05 (no conversion)"
        "2000-01-01T00:00+06:00" | 'GMT+05:00'       || "1999-12-31"          | "+06 to +05 (backwards conversion)"
    }

    void 'parse should convert an Excel date to a LocalDate for case: #scenario'() {
        given: 'the parser should ignore both the server and user timezones'
        sessionManagerStub.getTimezone() >> TimeZone.getTimeZone("Pacific/Kiritimati")  // +14:00

        and:
        DateParserContext context = new DateParserContext(epochDate: epochDate)

        expect:
        assert localDateParser.parse(excelDate, context) == LocalDate.parse(expected)

        where:
        excelDate | epochDate                  || expected     | scenario
        36527d    | EpochDate.UNIX_EPOCH       || "2000-01-02" | "A Double using the default (1900) Excel epoch"
        36527.0G  | EpochDate.UNIX_EPOCH       || "2000-01-02" | "A BigDecimal"
        36527.75d | EpochDate.UNIX_EPOCH       || "2000-01-02" | "A datetime late in the day keeps the same date"
        35065d    | EpochDate.EXCEL_1904       || "2000-01-02" | "A Double using the 1904 Excel epoch"
    }

    /**
     * Convenience method to build a Date for tests.
     */
    Date newDate(String date){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX").parse(date)
    }
}
