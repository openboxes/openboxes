package org.pih.warehouse.core.date

import java.time.ZoneOffset
import java.time.ZonedDateTime
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.localization.LocaleManager
import org.pih.warehouse.core.session.SessionManager

@Unroll
class ZonedDateTimeFormatterSpec extends Specification {

    static final ZonedDateTime MIDNIGHT_JAN_1ST_2000_UTC = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

    @Shared
    DatePatternLocalizer datePatternLocalizerStub

    @Shared
    LocaleManager localeManagerStub

    @Shared
    SessionManager sessionManagerStub

    @Shared
    ZonedDateTimeFormatter zonedDateTimeFormatter

    void setup() {
        zonedDateTimeFormatter = new ZonedDateTimeFormatter()
        datePatternLocalizerStub = Stub(DatePatternLocalizer)
        zonedDateTimeFormatter.datePatternLocalizer = datePatternLocalizerStub
        localeManagerStub = Stub(LocaleManager)
        zonedDateTimeFormatter.localeManager = localeManagerStub
        sessionManagerStub = Stub(SessionManager)
        zonedDateTimeFormatter.sessionManager = sessionManagerStub
    }

    void 'format can handle null inputs gracefully'() {
        expect:
        zonedDateTimeFormatter.format(null) == null
    }

    void 'format succeeds when given no format options'() {
        given:
        localeManagerStub.getCurrentLocale() >> Locale.ENGLISH

        and:
        sessionManagerStub.getTimezone() >> TimeZone.getTimeZone('Z')

        and:
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME, Locale.ENGLISH) >> 'dd/MMM/yyyy HH:mm:ss'

        expect:
        zonedDateTimeFormatter.format(MIDNIGHT_JAN_1ST_2000_UTC) == '01/Jan/2000 00:00:00'
    }

    void 'format succeeds when given a display format for scenario: #scenario'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withDisplayFormat(format)
                .build()

        and:
        localeManagerStub.getCurrentLocale() >> locale

        and:
        sessionManagerStub.getTimezone() >> TimeZone.getTimeZone(offset)

        and:
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME, Locale.ENGLISH) >> 'dd/MMM/yyyy HH:mm'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME, Locale.ITALIAN) >> 'MMM-dd-yyyy HH:mm'

        expect:
        zonedDateTimeFormatter.format(MIDNIGHT_JAN_1ST_2000_UTC, context) == expectedResult

        where:
        locale         | format                 | offset      || expectedResult              | scenario
        Locale.ENGLISH | DateDisplayFormat.GSP  | 'Z'         || '01/Jan/2000 00:00'         | 'GSP format localization in english UTC'
        Locale.ITALIAN | DateDisplayFormat.GSP  | 'Z'         || 'gen-01-2000 00:00'         | 'GSP format localization in non-english UTC'
        Locale.ENGLISH | DateDisplayFormat.CSV  | 'Z'         || '01/Jan/2000 00:00:00'      | 'CSV format localization (english) UTC'
        Locale.ITALIAN | DateDisplayFormat.CSV  | 'Z'         || '01/gen/2000 00:00:00'      | 'CSV format localization (non-english) UTC'
        Locale.ENGLISH | DateDisplayFormat.JSON | 'Z'         || '2000-01-01T00:00:00Z'      | 'JSON format uses ISO strings (english) UTC'
        Locale.ITALIAN | DateDisplayFormat.JSON | 'Z'         || '2000-01-01T00:00:00Z'      | 'JSON format uses ISO strings (non-english) UTC'
        Locale.ENGLISH | DateDisplayFormat.GSP  | 'GMT+01:00' || '01/Jan/2000 01:00'         | 'GSP format localization in english UTC+1'
        Locale.ITALIAN | DateDisplayFormat.GSP  | 'GMT+01:00' || 'gen-01-2000 01:00'         | 'GSP format localization in non-english UTC+1'
        Locale.ENGLISH | DateDisplayFormat.CSV  | 'GMT+01:00' || '01/Jan/2000 01:00:00'      | 'CSV format localization (english) UTC+1'
        Locale.ITALIAN | DateDisplayFormat.CSV  | 'GMT+01:00' || '01/gen/2000 01:00:00'      | 'CSV format localization (non-english) UTC+1'
        Locale.ENGLISH | DateDisplayFormat.JSON | 'GMT+01:00' || '2000-01-01T01:00:00+01:00' | 'JSON format uses ISO strings (english) UTC+1'
        Locale.ITALIAN | DateDisplayFormat.JSON | 'GMT+01:00' || '2000-01-01T01:00:00+01:00' | 'JSON format uses ISO strings (non-english) UTC+1'
    }

    void 'format succeeds when given a pattern override for scenario: #scenario'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withPatternOverride('yyyy.MMM.dd HH:mm XXX')
                .build()

        and:
        localeManagerStub.getCurrentLocale() >> locale

        and:
        sessionManagerStub.getTimezone() >> TimeZone.getTimeZone(offset)

        expect:
        zonedDateTimeFormatter.format(MIDNIGHT_JAN_1ST_2000_UTC, context) == expectedResult

        where:
        locale         | offset      || expectedResult             | scenario
        Locale.ENGLISH | 'Z'         || '2000.Jan.01 00:00 Z'      | 'format override localization in english'
        Locale.ITALIAN | 'Z'         || '2000.gen.01 00:00 Z'      | 'format override localization in non-english'
        Locale.ENGLISH | 'GMT+01:00' || '2000.Jan.01 01:00 +01:00' | 'GSP format localization in english UTC+1'
        Locale.ITALIAN | 'GMT+01:00' || '2000.gen.01 01:00 +01:00' | 'GSP format localization in non-english UTC+1'
    }

    void 'format succeeds when given a display style for scenario: #scenario'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withDisplayStyleOverride(style)
                .build()

        and:
        localeManagerStub.getCurrentLocale() >> locale

        and:
        sessionManagerStub.getTimezone() >> TimeZone.getTimeZone(offset)

        and:
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME, Locale.ENGLISH) >> 'dd/MMM/yyyy HH:mm'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME, Locale.ITALIAN) >> 'MMM-dd-yyyy HH:mm'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME_ZONE, Locale.ENGLISH) >> 'dd/MMM/yyyy HH:mm XXX'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME_ZONE, Locale.ITALIAN) >> 'MMM-dd-yyyy HH:mm XXX'

        expect:
        zonedDateTimeFormatter.format(MIDNIGHT_JAN_1ST_2000_UTC, context) == expectedResult

        where:
        locale         | style                           | offset      || expectedResult             | scenario
        Locale.ENGLISH | DateDisplayStyle.DATE_TIME      | 'Z'         || '01/Jan/2000 00:00'        | 'DATE_TIME style localization in english UTC'
        Locale.ITALIAN | DateDisplayStyle.DATE_TIME      | 'Z'         || 'gen-01-2000 00:00'        | 'DATE_TIME style localization in non-english UTC'
        Locale.ENGLISH | DateDisplayStyle.DATE_TIME_ZONE | 'Z'         || '01/Jan/2000 00:00 Z'      | 'DATE_TIME_ZONE style localization in english UTC'
        Locale.ITALIAN | DateDisplayStyle.DATE_TIME_ZONE | 'Z'         || 'gen-01-2000 00:00 Z'      | 'DATE_TIME_ZONE style localization in non-english UTC'
        Locale.ENGLISH | DateDisplayStyle.DATE_TIME      | 'GMT+01:00' || '01/Jan/2000 01:00'        | 'GSP format localization in english UTC+1'
        Locale.ITALIAN | DateDisplayStyle.DATE_TIME      | 'GMT+01:00' || 'gen-01-2000 01:00'        | 'GSP format localization in non-english UTC+1'
        Locale.ENGLISH | DateDisplayStyle.DATE_TIME_ZONE | 'GMT+01:00' || '01/Jan/2000 01:00 +01:00' | 'CSV format uses ISO strings (english) UTC+1'
        Locale.ITALIAN | DateDisplayStyle.DATE_TIME_ZONE | 'GMT+01:00' || 'gen-01-2000 01:00 +01:00' | 'CSV format uses ISO strings (non-english) UTC+1'
    }
}
