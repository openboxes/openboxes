package org.pih.warehouse.regression.platform

import org.pih.warehouse.*
import grails.testing.web.taglib.TagLibUnitTest
import groovy.time.TimeDuration
import org.grails.spring.beans.factory.InstanceFactoryBean
import org.grails.taglib.GrailsTagException
import spock.lang.Specification

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

import org.pih.warehouse.core.date.DateDisplayFormat
import org.pih.warehouse.core.date.DateDisplayStyle
import org.pih.warehouse.core.date.DateFormatter
import org.pih.warehouse.core.date.DateFormatterContext

class DateTagLibSpec extends Specification implements TagLibUnitTest<DateTagLib> {

    DateFormatter dateFormatter = Mock(DateFormatter)

    Closure doWithSpring() {{ ->
        grailsTagDateHelper(org.grails.plugins.web.DefaultGrailsTagDateHelper)
    }}

    void setupSpec() {
        mockTagLib(org.grails.plugins.web.taglib.FormatTagLib)
    }

    void setup() {
        DateFormatter formatter = dateFormatter
        defineBeans {
            dateFormatter(InstanceFactoryBean, formatter, DateFormatter)
        }
    }

    void 'copyrightYear is the current year'() {
        expect:
        applyTemplate('<g:copyrightYear/>') == new Date().format('yyyy')
    }

    void 'formatDate requires a date attribute and rejects unsupported types'() {
        when:
        tagLib.formatDate([:])

        then:
        GrailsTagException e = thrown()
        e.message.contains('[date] is required')

        when:
        tagLib.formatDate(date: 'yesterday')

        then:
        e = thrown(GrailsTagException)
        e.message.contains('does not support type [class java.lang.String]')
    }

    void 'formatDate renders nothing for a missing date'() {
        expect:
        tagLib.formatDate(date: null).toString() == ''
    }

    void 'formatDate formats a java.util.Date with the default pattern in the session timezone'() {
        given:
        Date noonUtc = Date.from(Instant.parse('2026-03-05T12:00:00Z'))
        session.timezone = TimeZone.getTimeZone('America/New_York')

        expect:
        tagLib.formatDate(date: noonUtc).toString() == '05/Mar/2026 07:00:00'
        tagLib.formatDate(date: noonUtc, format: 'yyyy-MM-dd HH:mm', timeZone: TimeZone.getTimeZone('UTC')).toString() ==
                '2026-03-05 12:00'
    }

    void 'formatDate hands java.time values to the date formatter with the GSP display format'() {
        given:
        LocalDate day = LocalDate.of(2026, 3, 5)

        when:
        String out = tagLib.formatDate(date: day, locale: Locale.FRENCH, timeZone: ZoneId.of('UTC')).toString()

        then:
        1 * dateFormatter.format(day, { DateFormatterContext c ->
            c.localeOverride == Locale.FRENCH && c.timezoneOverride == ZoneId.of('UTC') &&
                    c.displayFormat == DateDisplayFormat.GSP && c.patternOverride == null
        }) >> '5 mars 2026'
        out == '5 mars 2026'
    }

    void 'formatDate passes a pattern or display style override instead of the display format'() {
        given:
        Instant now = Instant.parse('2026-03-05T12:00:00Z')

        when:
        tagLib.formatDate(date: now, format: 'yyyy')
        tagLib.formatDate(date: now, displayStyle: DateDisplayStyle.values()[0])

        then:
        1 * dateFormatter.format(now, { it.patternOverride == 'yyyy' && it.displayFormat == null }) >> '2026'
        1 * dateFormatter.format(now, { it.displayStyleOverride == DateDisplayStyle.values()[0] && it.displayFormat == null }) >> 'x'
    }

    void 'expirationDate renders the expiration date template with the attributes'() {
        given:
        views['/taglib/_expirationDate.gsp'] = 'expires ${attrs.date}'

        expect:
        applyTemplate('<g:expirationDate date="2026"/>') == 'expires 2026'
    }

    void 'relativeTime shows the largest non-zero unit of a duration'() {
        expect:
        applyTemplate('<g:relativeTime timeDuration="${d}"/>', [d: duration]) == expected

        where:
        duration                             || expected
        new TimeDuration(3, 4, 0, 0, 0)      || '3 days'
        new TimeDuration(0, 4, 5, 0, 0)      || '4 hours'
        new TimeDuration(0, 0, 5, 6, 0)      || '5 minutes'
        new TimeDuration(0, 0, 0, 6, 0)      || '6 seconds'
        new TimeDuration(0, 0, 0, 0, 10)     || "<span class='fade'>none</span>"
        null                                 || "<span class='fade'>none</span>"
    }

    void 'relativeTime shows years with the remaining days'() {
        given:
        TimeDuration twoYears = new TimeDuration(12, 0, 0, 0, 0) {
            int getYears() { 2 }
        }

        expect:
        applyTemplate('<g:relativeTime timeDuration="${d}"/>', [d: twoYears]) == '2 years, 12 days'
    }

    void 'prettyDateFormat describes a date relative to now'() {
        expect:
        applyTemplate('<g:prettyDateFormat date="${d}"/>', [d: new Date() - 3]) == '3 days ago'
        applyTemplate('<g:prettyDateFormat date="${null}"/>') == ''
    }
}
