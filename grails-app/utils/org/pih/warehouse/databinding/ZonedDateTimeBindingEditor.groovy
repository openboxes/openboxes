package org.pih.warehouse.databinding

import java.time.ZonedDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.date.ZonedDateTimeParser
import org.pih.warehouse.core.session.SessionManager

@Component
class ZonedDateTimeBindingEditor extends CustomDateBindingEditor<ZonedDateTime> {

    @Autowired
    SessionManager sessionManager

    @Autowired
    ZonedDateTimeParser zonedDateTimeParser

    @Override
    ZonedDateTime getDate(Calendar c) {
        // When binding input, we always default to assuming we've been given a datetime in the user's timezone.
        // Grails' date picker does not allow you to specify the timezone. The Calendar instance we get here
        // will always be in the timezone of the server, so we need to manually set it to be in the user's timezone.
        c.timeZone = sessionManager.timezone
        return zonedDateTimeParser.parse(c)
    }

    @Override
    Class<?> getTargetType() {
        ZonedDateTime
    }
}
