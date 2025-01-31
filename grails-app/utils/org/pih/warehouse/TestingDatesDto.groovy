package org.pih.warehouse

import grails.validation.Validateable
import java.time.Instant
import java.time.LocalDate

class TestingDatesDto implements Validateable {
    Instant myInstant
    LocalDate myLocalDate
    Date myDate

    static constraints = {
        myInstant(nullable: true)
        myLocalDate(nullable: true)
        myDate(nullable: true)
    }

    Map toJson() {
        return [
                myInstant: myInstant.toString(),
                myLocalDate: myLocalDate.toString(),
                myDate: myDate,
        ]
    }
}
