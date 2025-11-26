package org.pih.warehouse.core

/**
 * Used to filter the returned results when listing/searching internal receiving bin locations.
 */
class ReceivingLocationSearchCommand extends InternalLocationSearchCommand {

    String shipmentNumber

    static constraints = {
        location(nullable: true)
        locationTypeCode(nullable: true)
        allActivityCodes(nullable: true)
        anyActivityCodes(nullable: true)
        ignoreActivityCodes(nullable: true)
    }
}
