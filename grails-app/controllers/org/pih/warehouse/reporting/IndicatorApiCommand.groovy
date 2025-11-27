package org.pih.warehouse.reporting

import grails.validation.Validateable
import java.time.Instant

import org.pih.warehouse.core.Location

class IndicatorApiCommand implements Validateable {
    Location facility
    Instant startDate
    Instant endDate
}
