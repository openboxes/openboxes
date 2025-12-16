package unit.org.pih.warehouse.core.date

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.date.DateDisplayFormat
import org.pih.warehouse.core.date.DateFormatterContext
import org.pih.warehouse.core.date.DateFormatter
import org.pih.warehouse.core.date.JavaUtilDateFormatter
import org.pih.warehouse.core.date.TemporalAccessorDateFormatter
import org.pih.warehouse.core.date.TemporalAccessorDateTimeFormatter
import org.pih.warehouse.core.localization.LocaleManager
import org.pih.warehouse.core.session.SessionManager

/**
 * Tests the DateFormatter.
 * We don't test the specific formatters themselves. We let those be tested separately.
 */
@Unroll
class DateFormatterSpec extends Specification {

    // We don't care that this isn't a real date since we're not testing the formatters themselves
    static final String FORMATTED_DATE_STRING = 'FORMATTED!'

    @Shared
    DateFormatter dateFormatter

    @Shared
    SessionManager sessionManagerStub

    @Shared
    LocaleManager localeManagerStub

    void setup() {
        // Spy because we need to stub the formatter init methods.
        // We don't actually care what the formatters do, so we simply stub them to always return the same thing.
        dateFormatter = Spy(DateFormatter) {
            initInstantFormatter(_, _, _, _) >> Stub(TemporalAccessorDateTimeFormatter) {
                format(_) >> FORMATTED_DATE_STRING
            }
            initZonedDateTimeFormatter(_, _, _, _) >> Stub(TemporalAccessorDateTimeFormatter) {
                format(_) >> FORMATTED_DATE_STRING
            }
            initLocalDateFormatter(_, _, _) >> Stub(TemporalAccessorDateFormatter) {
                format(_) >> FORMATTED_DATE_STRING
            }
            initJavaUtilDateFormatter() >> Stub(JavaUtilDateFormatter) {
                format(_) >> FORMATTED_DATE_STRING
            }
        }

        sessionManagerStub = Stub(SessionManager)
        dateFormatter.sessionManager = sessionManagerStub

        localeManagerStub = Stub(LocaleManager)
        dateFormatter.localeManager = localeManagerStub
    }

    void 'format returns null when given a null date'() {
        expect:
        dateFormatter.format(null) == null
    }

    void 'format returns #expectedValue when given a null date and a default value of #defaultValue'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withDefaultValue(defaultValue)
                .build()

        expect:
        dateFormatter.format(null, context) == expectedValue

        where:
        defaultValue | expectedValue
        null         | null
        ''           | ''
        'DEFAULT'    | 'DEFAULT'
    }

    void 'format does not error when given an Instant and no override'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone('UTC')
        localeManagerStub.currentLocale >> Locale.ENGLISH

        and:
        Instant instant = Instant.now()

        expect:
        dateFormatter.format(instant) == FORMATTED_DATE_STRING
    }

    void 'format does not error when given an Instant and overrides'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withLocaleOverride(Locale.ENGLISH)
                .withTimezoneOverride(ZoneId.of('+01:00'))
                .withDisplayFormat(DateDisplayFormat.GSP)
                .build()

        and:
        Instant instant = Instant.now()

        expect:
        dateFormatter.format(instant, context) == FORMATTED_DATE_STRING
    }

    void 'format does not error when given a ZonedDateTime and no override'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone('UTC')
        localeManagerStub.currentLocale >> Locale.ENGLISH

        and:
        ZonedDateTime zdt = ZonedDateTime.now()

        expect:
        dateFormatter.format(zdt) == FORMATTED_DATE_STRING
    }

    void 'format does not error when given a ZonedDateTime and overrides'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withLocaleOverride(Locale.ENGLISH)
                .withTimezoneOverride(ZoneId.of('+01:00'))
                .withDisplayFormat(DateDisplayFormat.GSP)
                .build()

        and:
        ZonedDateTime zdt = ZonedDateTime.now()

        expect:
        dateFormatter.format(zdt, context) == FORMATTED_DATE_STRING
    }

    void 'format does not error when given a LocalDate and no override'() {
        given:
        localeManagerStub.currentLocale >> Locale.ENGLISH

        and:
        LocalDate localDate = LocalDate.now()

        expect:
        dateFormatter.format(localDate) == FORMATTED_DATE_STRING
    }

    void 'format does not error when given a LocalDate and overrides'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withLocaleOverride(Locale.ENGLISH)
                .withTimezoneOverride(ZoneId.of('+01:00'))  // Does nothing for LocalDate
                .withDisplayFormat(DateDisplayFormat.GSP)
                .build()

        and:
        LocalDate localDate = LocalDate.now()

        expect:
        dateFormatter.format(localDate, context) == FORMATTED_DATE_STRING
    }

    void 'format does not error when given a Date and overrides'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withLocaleOverride(Locale.ENGLISH)
                .withTimezoneOverride(ZoneId.of('+01:00'))
                .withDisplayFormat(DateDisplayFormat.GSP)
                .build()

        and:
        Date date = new Date()

        expect:
        dateFormatter.format(date, context) == FORMATTED_DATE_STRING
    }

    void 'format does not error when given a Date and no override'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone('UTC')
        localeManagerStub.currentLocale >> Locale.ENGLISH

        and:
        Date date = new Date()

        expect:
        dateFormatter.format(date) == FORMATTED_DATE_STRING
    }
}
