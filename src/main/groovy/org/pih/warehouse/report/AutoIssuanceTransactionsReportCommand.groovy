package org.pih.warehouse.report

import org.pih.warehouse.core.Location

class AutoIssuanceTransactionsReportCommand extends CycleCountReportCommand {

    List<Location> binLocations
    String sort
    String order

    static constraints = {
        binLocations(nullable: true)
        sort(nullable: true)
        order(nullable: true)
    }
}
