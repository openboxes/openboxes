package unit.org.pih.warehouse.inventory

import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.CycleCountItem
import org.pih.warehouse.inventory.InventoryItem
import spock.lang.Specification

class CycleCountItemSpec extends Specification implements DomainUnitTest<CycleCountItem> {

    void 'getQuantityVariance should return #quantityVariance when quantityCounted is #quantityCounted and qoh is #qoh'() {
        given:
        domain.quantityCounted = quantityCounted
        domain.quantityOnHand = quantityOnHand

        expect:
        domain.quantityVariance == quantityVariance

        where:
        quantityCounted | quantityOnHand || quantityVariance
        2               | 2              || 0
        10              | 3              || 7
        10              | 20             || -10
        null            | 1              || null
        1               | null           || null
    }

    void 'compareTo should return items in order starting with expiration date and location name'() {
        given:
        CycleCountItem cycleCountItem1 = new CycleCountItem(
                inventoryItem: new InventoryItem(
                        expirationDate: new Date(2025, 03, 02)
                ),
                location: new Location()
        )
        CycleCountItem cycleCountItem2 = new CycleCountItem(
                inventoryItem: new InventoryItem(
                        expirationDate: new Date(2025, 02, 02),
                ),
                location: new Location(
                        name: 'A',
                )
        )
        CycleCountItem cycleCountItem3 = new CycleCountItem(
                inventoryItem: new InventoryItem(
                        expirationDate: new Date(2025, 02, 02),
                ),
                location: new Location(
                        name: 'B',
                )
        )
        CycleCountItem cycleCountItem4 = new CycleCountItem(
                inventoryItem: new InventoryItem(
                        expirationDate: new Date(2025, 01, 02),
                ),
                location: new Location(
                        name: 'C',
                )
        )
        CycleCountItem cycleCountItem5 = new CycleCountItem(
                inventoryItem: new InventoryItem(
                        expirationDate: new Date(2025, 01, 02),
                ),
                location: new Location(
                        name: 'C',
                )
        )

        and:
        List<CycleCountItem> cycleCountItems = [
                cycleCountItem5,
                cycleCountItem4,
                cycleCountItem3,
                cycleCountItem2,
                cycleCountItem1,
        ]

        when:
        cycleCountItems.sort()

        then:
        cycleCountItems[0] == cycleCountItem5
        cycleCountItems[1] == cycleCountItem4
        cycleCountItems[2] == cycleCountItem2
        cycleCountItems[3] == cycleCountItem3
        cycleCountItems[4] == cycleCountItem1
    }
}
