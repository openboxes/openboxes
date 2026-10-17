package org.pih.warehouse.regression.platform.databinding

import org.pih.warehouse.databinding.*
import grails.databinding.SimpleMapDataBindingSource
import java.time.Instant
import spock.lang.Specification

import org.pih.warehouse.core.date.InstantParser
import org.pih.warehouse.core.session.SessionManager

class InstantBindingEditorSpec extends Specification {

    InstantBindingEditor editor

    void setup() {
        SessionManager sessionManager = Stub(SessionManager) {
            getTimezone() >> TimeZone.getTimeZone('America/New_York')
        }
        editor = new InstantBindingEditor(sessionManager: sessionManager, instantParser: new InstantParser(sessionManager: sessionManager))
    }

    void 'the editor targets Instant'() {
        expect:
        assert editor.targetType == Instant
    }

    void 'getDate should interpret the calendar fields in the user timezone'() {
        given: 'a calendar built in a different timezone than the user'
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone('Asia/Tokyo'))
        calendar.clear()
        calendar.set(2024, Calendar.JANUARY, 15, 10, 30)

        expect:
        assert editor.getDate(calendar) == Instant.parse('2024-01-15T15:30:00Z')
    }

    void 'assemble should bind date picker params as a datetime in the user timezone'() {
        given:
        SimpleMapDataBindingSource source = new SimpleMapDataBindingSource([
                startDate       : 'date.struct',
                startDate_year  : '2024',
                startDate_month : '7',
                startDate_day   : '4',
                startDate_hour  : '09',
                startDate_minute: '15',
        ])

        expect: 'New York is on daylight time (UTC-4) in July'
        assert editor.assemble('startDate', source) == Instant.parse('2024-07-04T13:15:00Z')
    }
}
