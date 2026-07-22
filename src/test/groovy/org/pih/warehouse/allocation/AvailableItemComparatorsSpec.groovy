package org.pih.warehouse.allocation

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.inventory.InventoryItem
import spock.lang.Specification

class AvailableItemComparatorsSpec extends Specification {

    private static AvailableItem withExpiration(Date expirationDate) {
        return new AvailableItem(inventoryItem: new InventoryItem(expirationDate: expirationDate))
    }

    void "BY_EXPIRATION_NULLS_LAST orders earliest expiration first and missing dates last"() {
        given:
        AvailableItem noExpiry = withExpiration(null)
        AvailableItem later = withExpiration(new Date(2_000))
        AvailableItem earlier = withExpiration(new Date(1_000))

        expect:
        [noExpiry, later, earlier].sort(false, AvailableItemComparators.BY_EXPIRATION_NULLS_LAST) ==
                [earlier, later, noExpiry]
    }

    void "BY_EXPIRATION_NULLS_LAST treats two missing expiration dates as equal"() {
        expect:
        AvailableItemComparators.BY_EXPIRATION_NULLS_LAST.compare(withExpiration(null), withExpiration(null)) == 0
    }
}
