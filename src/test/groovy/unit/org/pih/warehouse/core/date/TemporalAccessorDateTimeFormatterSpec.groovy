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

    static final Instant MIDNIGHT_JAN_1ST_2000 = Instant.from(ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))

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
        formatter.format(MIDNIGHT_JAN_1ST_2000)

        then:
        thrown(IllegalArgumentException)
    }

    void 'format succeeds when given a display format for scenario: #scenario'() {
        given:
        TemporalAccessorDateTimeFormatter formatter = initFormatter(locale, format, null, null, ZoneOffset.UTC)

        and:
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME, Locale.ENGLISH) >> 'dd/MMM/yyyy HH:mm'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME, Locale.ITALIAN) >> 'MMM-dd-yyyy HH:mm'

        expect:
        formatter.format(MIDNIGHT_JAN_1ST_2000) == expectedResult

        where:
        locale         | format                 || expectedResult         | scenario
        Locale.ENGLISH | DateDisplayFormat.GSP  || '01/Jan/2000 00:00'    | 'GSP format localization in english'
        Locale.ITALIAN | DateDisplayFormat.GSP  || 'gen-01-2000 00:00'    | 'GSP format localization in non-english'
        Locale.ENGLISH | DateDisplayFormat.CSV  || '2000-01-01T00:00:00Z' | 'CSV format uses ISO strings (english)'
        Locale.ITALIAN | DateDisplayFormat.CSV  || '2000-01-01T00:00:00Z' | 'CSV format uses ISO strings (non-english)'
        Locale.ENGLISH | DateDisplayFormat.JSON || '2000-01-01T00:00:00Z' | 'JSON format uses ISO strings (english)'
        Locale.ITALIAN | DateDisplayFormat.JSON || '2000-01-01T00:00:00Z' | 'JSON format uses ISO strings (non-english)'
    }

    void 'format succeeds when given a pattern override for scenario: #scenario'() {
        given: 'a formatter using the DATE style (see setup for where the DATE patterns are mocked)'
        TemporalAccessorDateTimeFormatter formatter = initFormatter(locale, null, 'yyyy.MMM.dd', null, ZoneOffset.UTC)

        expect:
        formatter.format(MIDNIGHT_JAN_1ST_2000) == expectedResult

        where:
        locale         || expectedResult | scenario
        Locale.ENGLISH || '2000.Jan.01'  | 'format override localization in english'
        Locale.ITALIAN || '2000.gen.01'  | 'format override localization in non-english'
    }

    void 'format succeeds when given a display style for scenario: #scenario'() {
        given: 'a formatter using the DATE style (see setup for where the DATE patterns are mocked)'
        TemporalAccessorDateTimeFormatter formatter = initFormatter(locale, null, null, style, ZoneOffset.UTC)

        and:
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME, Locale.ENGLISH) >> 'dd/MMM/yyyy HH:mm'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME, Locale.ITALIAN) >> 'MMM-dd-yyyy HH:mm'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME_ZONE, Locale.ENGLISH) >> 'dd/MMM/yyyy HH:mm XXX'
        datePatternLocalizerStub.localizePattern(DateDisplayStyle.DATE_TIME_ZONE, Locale.ITALIAN) >> 'MMM-dd-yyyy HH:mm XXX'

        expect:
        formatter.format(MIDNIGHT_JAN_1ST_2000) == expectedResult

        where:
        locale         | style                           || expectedResult        | scenario
        Locale.ENGLISH | DateDisplayStyle.DATE_TIME      || '01/Jan/2000 00:00'   | 'DATE_TIME style localization in english'
        Locale.ITALIAN | DateDisplayStyle.DATE_TIME      || 'gen-01-2000 00:00'   | 'DATE_TIME style localization in non-english'
        Locale.ENGLISH | DateDisplayStyle.DATE_TIME_ZONE || '01/Jan/2000 00:00 Z' | 'DATE_TIME_ZONE style localization in english'
        Locale.ITALIAN | DateDisplayStyle.DATE_TIME_ZONE || 'gen-01-2000 00:00 Z' | 'DATE_TIME_ZONE style localization in non-english'
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
