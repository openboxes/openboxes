package org.pih.warehouse

import java.time.Instant
import java.time.LocalDate

class TestingDates implements Serializable {

    String id
    Instant myInstant
    LocalDate myLocalDate
    Date myDate

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        myInstant(nullable: true)
        myLocalDate(nullable: true)
        myDate(nullable: true)
    }

    Map toJson() {
        return [
                id: id,
                myInstant: myInstant.toString(),
                myLocalDate: myLocalDate.toString(),
                myDate: myDate,
                myDateToString: myDate.toString(),
        ]
    }
}
