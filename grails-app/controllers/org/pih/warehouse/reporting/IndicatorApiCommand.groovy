package org.pih.warehouse.reporting

import grails.validation.Validateable
import org.pih.warehouse.core.Location

class IndicatorApiCommand implements Validateable {
    Location facility
    Date startDate
    Date endDate
}
