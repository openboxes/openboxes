package unit.org.pih.warehouse.core.date

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.date.DateDisplayFormat
import org.pih.warehouse.core.date.DateDisplayStyle
import org.pih.warehouse.core.date.DatePatternLocalizer
import org.pih.warehouse.core.date.TemporalAccessorDateTimeFormatter

@Unroll
class TemporalAccessorDateTimeFormatterSpec extends Specification {

    static final Instant MIDNIGHT_JAN_1ST_2000_UTC =
            Instant.from(ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))

    @Shared
    DatePatternLocalizer datePatternLocalizerStub

    void setup() {
        datePatternLocalizerStub = Stub(DatePatternLocalizer)
    }

    void 'format can handle null inputs gracefully'() {
        given:
        TemporalAccessorDateTimeFormatter formatter = initFormatter(Locale.ENGLISH, null, null, null, ZoneOffset.UTC)

        expect:
        formatter.format(null) == ''
    }

    void 'format errors when given no format options'() {
        given:
        TemporalAccessorDateTimeFormatter formatter = initFormatter(Locale.ENGLISH, null, null, null, ZoneOffset.UTC)

        when:
        formatter.format(MIDNIGHT_JAN_1ST_2000_UTC)

        then:
        thrown(IllegalArgumentException)
    }

    void 'format succeeds when given a display format for scenario: #scenario'() {
        given:
        TemporalAccessorDateTimeFormatter formatter = initFormatter(locale, format, null, null, ZoneId.of(offset))

        and:
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME, Locale.ENGLISH) >> 'dd/MMM/yyyy HH:mm'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME, Locale.ITALIAN) >> 'MMM-dd-yyyy HH:mm'

        expect:
        formatter.format(MIDNIGHT_JAN_1ST_2000_UTC) == expectedResult

        where:
        locale         | format                 | offset   || expectedResult              | scenario
        Locale.ENGLISH | DateDisplayFormat.GSP  | 'Z'      || '01/Jan/2000 00:00'         | 'GSP format localization in english UTC'
        Locale.ITALIAN | DateDisplayFormat.GSP  | 'Z'      || 'gen-01-2000 00:00'         | 'GSP format localization in non-english UTC'
        Locale.ENGLISH | DateDisplayFormat.CSV  | 'Z'      || '2000-01-01T00:00:00Z'      | 'CSV format uses ISO strings (english) UTC'
        Locale.ITALIAN | DateDisplayFormat.CSV  | 'Z'      || '2000-01-01T00:00:00Z'      | 'CSV format uses ISO strings (non-english) UTC'
        Locale.ENGLISH | DateDisplayFormat.JSON | 'Z'      || '2000-01-01T00:00:00Z'      | 'JSON format uses ISO strings (english) UTC'
        Locale.ITALIAN | DateDisplayFormat.JSON | 'Z'      || '2000-01-01T00:00:00Z'      | 'JSON format uses ISO strings (non-english) UTC'
        Locale.ENGLISH | DateDisplayFormat.GSP  | '+01:00' || '01/Jan/2000 01:00'         | 'GSP format localization in english UTC+1'
        Locale.ITALIAN | DateDisplayFormat.GSP  | '+01:00' || 'gen-01-2000 01:00'         | 'GSP format localization in non-english UTC+1'
        Locale.ENGLISH | DateDisplayFormat.CSV  | '+01:00' || '2000-01-01T01:00:00+01:00' | 'CSV format uses ISO strings (english) UTC+1'
        Locale.ITALIAN | DateDisplayFormat.CSV  | '+01:00' || '2000-01-01T01:00:00+01:00' | 'CSV format uses ISO strings (non-english) UTC+1'
        Locale.ENGLISH | DateDisplayFormat.JSON | '+01:00' || '2000-01-01T01:00:00+01:00' | 'JSON format uses ISO strings (english) UTC+1'
        Locale.ITALIAN | DateDisplayFormat.JSON | '+01:00' || '2000-01-01T01:00:00+01:00' | 'JSON format uses ISO strings (non-english) UTC+1'
    }

    void 'format succeeds when given a pattern override for scenario: #scenario'() {
        given:
        String pattern = 'yyyy.MMM.dd HH:mm XXX'
        TemporalAccessorDateTimeFormatter formatter = initFormatter(locale, null, pattern, null, ZoneId.of(offset))

        expect:
        formatter.format(MIDNIGHT_JAN_1ST_2000_UTC) == expectedResult

        where:
        locale         | offset   || expectedResult             | scenario
        Locale.ENGLISH | 'Z'      || '2000.Jan.01 00:00 Z'      | 'format override localization in english'
        Locale.ITALIAN | 'Z'      || '2000.gen.01 00:00 Z'      | 'format override localization in non-english'
        Locale.ENGLISH | '+01:00' || '2000.Jan.01 01:00 +01:00' | 'GSP format localization in english UTC+1'
        Locale.ITALIAN | '+01:00' || '2000.gen.01 01:00 +01:00' | 'GSP format localization in non-english UTC+1'
    }

    void 'format succeeds when given a display style for scenario: #scenario'() {
        given:
        TemporalAccessorDateTimeFormatter formatter = initFormatter(locale, null, null, style, ZoneId.of(offset))

        and:
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME, Locale.ENGLISH) >> 'dd/MMM/yyyy HH:mm'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME, Locale.ITALIAN) >> 'MMM-dd-yyyy HH:mm'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME_ZONE, Locale.ENGLISH) >> 'dd/MMM/yyyy HH:mm XXX'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME_ZONE, Locale.ITALIAN) >> 'MMM-dd-yyyy HH:mm XXX'

        expect:
        formatter.format(MIDNIGHT_JAN_1ST_2000_UTC) == expectedResult

        where:
        locale         | style                           | offset   || expectedResult             | scenario
        Locale.ENGLISH | DateDisplayStyle.DATE_TIME      | 'Z'      || '01/Jan/2000 00:00'        | 'DATE_TIME style localization in english UTC'
        Locale.ITALIAN | DateDisplayStyle.DATE_TIME      | 'Z'      || 'gen-01-2000 00:00'        | 'DATE_TIME style localization in non-english UTC'
        Locale.ENGLISH | DateDisplayStyle.DATE_TIME_ZONE | 'Z'      || '01/Jan/2000 00:00 Z'      | 'DATE_TIME_ZONE style localization in english UTC'
        Locale.ITALIAN | DateDisplayStyle.DATE_TIME_ZONE | 'Z'      || 'gen-01-2000 00:00 Z'      | 'DATE_TIME_ZONE style localization in non-english UTC'
        Locale.ENGLISH | DateDisplayStyle.DATE_TIME      | '+01:00' || '01/Jan/2000 01:00'        | 'GSP format localization in english UTC+1'
        Locale.ITALIAN | DateDisplayStyle.DATE_TIME      | '+01:00' || 'gen-01-2000 01:00'        | 'GSP format localization in non-english UTC+1'
        Locale.ENGLISH | DateDisplayStyle.DATE_TIME_ZONE | '+01:00' || '01/Jan/2000 01:00 +01:00' | 'CSV format uses ISO strings (english) UTC+1'
        Locale.ITALIAN | DateDisplayStyle.DATE_TIME_ZONE | '+01:00' || 'gen-01-2000 01:00 +01:00' | 'CSV format uses ISO strings (non-english) UTC+1'
    }

    private TemporalAccessorDateTimeFormatter<Instant> initFormatter(
            Locale locale,
            DateDisplayFormat displayFormat,
            String patternOverride,
            DateDisplayStyle displayStyleOverride,
            ZoneId zoneId) {

        TemporalAccessorDateTimeFormatter<Instant> formatter = Spy(new TemporalAccessorDateTimeFormatter(
                locale, displayFormat, patternOverride, displayStyleOverride, zoneId))

        formatter.getLocalizer() >> datePatternLocalizerStub

        return formatter
    }
}
