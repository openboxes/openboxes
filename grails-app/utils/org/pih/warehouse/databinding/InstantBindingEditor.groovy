package org.pih.warehouse.databinding

import java.time.Instant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.session.SessionManager

@Component
class InstantBindingEditor extends CustomDateBindingEditor<Instant> {

    @Autowired
    SessionManager sessionManager

    @Override
    Instant getDate(Calendar c) {
        // When binding input, we always default to assuming we've been given a datetime in the user's timezone.
        // Grails' date picker does not allow you to specify the timezone, so the Calendar instance we get here
        // will always be in the timezone of the server.
        c.timeZone = sessionManager.timezone
        return c.toInstant()
    }

    @Override
    Class<?> getTargetType() {
        Instant
    }
}
