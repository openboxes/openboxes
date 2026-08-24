package org.pih.warehouse.inventory

import java.time.LocalDate

import org.pih.warehouse.DateUtil
import org.pih.warehouse.core.date.LocalDateParser

/**
 * A product lot with the depots holding it.
 */
class LotAvailabilityDto {

    // A date-only LocalDate (not Date) so it serializes as e.g. "2028-03-01" instead of an instant that the server's
    // timezone offset can shift to the previous day (e.g. "2028-02-29T23:00:00Z"). See the LocalDate JSON marshaller.
    LocalDate expirationDate

    // The total quantity of the lot across all depots.
    Integer quantityOnHand = 0

    List<DepotAvailabilityDto> depots = []

    static LotAvailabilityDto from(InventoryItem inventoryItem, List<DepotAvailabilityDto> depots) {
        return !inventoryItem ? null: new LotAvailabilityDto(
                expirationDate: LocalDateParser.asLocalDate(inventoryItem.expirationDate, DateUtil.systemZoneId),
                quantityOnHand: depots.sum(0) { it.quantityOnHand } as Integer,
                depots: depots,
        )
    }
}
