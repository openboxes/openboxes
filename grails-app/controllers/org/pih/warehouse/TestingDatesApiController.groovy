package org.pih.warehouse

import grails.converters.JSON

class TestingDatesApiController {

    def save(TestingDates dates) {
        TestingDates savedDates = dates.save(failOnError: true)
        render([data: savedDates] as JSON)
    }
}
