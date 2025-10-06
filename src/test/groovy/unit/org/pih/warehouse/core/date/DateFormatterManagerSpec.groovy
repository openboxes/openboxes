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
import org.pih.warehouse.core.date.DateFormatterManager
import org.pih.warehouse.core.date.TemporalAccessorDateFormatter
import org.pih.warehouse.core.date.TemporalAccessorDateTimeFormatter
import org.pih.warehouse.core.localization.LocaleDeterminer
import org.pih.warehouse.core.session.SessionManager

/**
 * Tests the DateFormatterManager.
 * We don't test the specific formatters themselves. We let those be tested separately.
 */
@Unroll
class DateFormatterManagerSpec extends Specification {

    // We don't care that this isn't a real date since we're not testing the formatters themselves
    static final String FORMATTED_DATE_STRING = 'FORMATTED!'

    @Shared
    DateFormatterManager dateFormatterManager

    @Shared
    SessionManager sessionManagerStub

    @Shared
    LocaleDeterminer localeDeterminerStub

    void setup() {
        // Spy because we need to stub the formatter init methods.
        // We don't actually care what formatters do, so we simply stub them to always return the same thing.
        dateFormatterManager = Spy(DateFormatterManager) {
            initInstantFormatter(_, _, _, _) >> Stub(TemporalAccessorDateTimeFormatter) {
                format(_) >> FORMATTED_DATE_STRING
            }
            initZonedDateTimeFormatter(_, _, _, _) >> Stub(TemporalAccessorDateTimeFormatter) {
                format(_) >> FORMATTED_DATE_STRING
            }
            initLocalDateFormatter(_, _, _) >> Stub(TemporalAccessorDateFormatter) {
                format(_) >> FORMATTED_DATE_STRING
            }
        }

        sessionManagerStub = Stub(SessionManager)
        dateFormatterManager.sessionManager = sessionManagerStub

        localeDeterminerStub = Stub(LocaleDeterminer)
        dateFormatterManager.localeDeterminer = localeDeterminerStub
    }

    void 'format returns null when given a null date'() {
        expect:
        dateFormatterManager.format(null) == null
    }

    void 'format returns the default value when given a null date and a default is specified'() {
        given:
        DateFormatterContext context = DateFormatterContext.builder()
                .withDefaultValue('DEFAULT')
                .build()

        expect:
        dateFormatterManager.format(null, context) == 'DEFAULT'
    }

    void 'format does not error when given an Instant and no override'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone('UTC')
        localeDeterminerStub.currentLocale >> Locale.ENGLISH

        and:
        Instant instant = Instant.now()

        expect:
        dateFormatterManager.format(instant) == FORMATTED_DATE_STRING
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
        dateFormatterManager.format(instant, context) == FORMATTED_DATE_STRING
    }

    void 'format does not error when given a ZonedDateTime and no override'() {
        given:
        sessionManagerStub.timezone >> TimeZone.getTimeZone('UTC')
        localeDeterminerStub.currentLocale >> Locale.ENGLISH

        and:
        ZonedDateTime zdt = ZonedDateTime.now()

        expect:
        dateFormatterManager.format(zdt) == FORMATTED_DATE_STRING
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
        dateFormatterManager.format(zdt, context) == FORMATTED_DATE_STRING
    }

    void 'format does not error when given a LocalDate and no override'() {
        given:
        localeDeterminerStub.currentLocale >> Locale.ENGLISH

        and:
        LocalDate localDate = LocalDate.now()

        expect:
        dateFormatterManager.format(localDate) == FORMATTED_DATE_STRING
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
        dateFormatterManager.format(localDate, context) == FORMATTED_DATE_STRING
    }
}
