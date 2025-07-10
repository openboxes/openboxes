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

    void 'sort should sort items by expiration date, then location name, then id'() {
        given:
        CycleCountItem cycleCountItem0 = new CycleCountItem(
                inventoryItem: new InventoryItem(
                        expirationDate: new Date(2025, 01, 01),
                ),
                location: new Location(
                        name: 'C',
                )
        )
        cycleCountItem0.id = 10

        CycleCountItem cycleCountItem1 = new CycleCountItem(
                inventoryItem: new InventoryItem(
                        expirationDate: new Date(2025, 01, 01),
                ),
                location: new Location(
                        name: 'C',
                )
        )
        cycleCountItem1.id = 11

        CycleCountItem cycleCountItem2 = new CycleCountItem(
                inventoryItem: new InventoryItem(
                        expirationDate: new Date(2025, 01, 01),
                ),
                location: new Location(
                        name: 'D',
                )
        )

        cycleCountItem2.id = 3
        CycleCountItem cycleCountItem3 = new CycleCountItem(
                inventoryItem: new InventoryItem(
                        expirationDate: new Date(2025, 01, 02),
                ),
                location: new Location(
                        name: 'A',
                )
        )
        cycleCountItem3.id = 2

        CycleCountItem cycleCountItem4 = new CycleCountItem(
                inventoryItem: new InventoryItem(
                        expirationDate: new Date(2025, 01, 02),
                ),
                location: new Location(
                        name: 'B',
                )
        )
        cycleCountItem4.id = 1

        and: 'a list of items that is unsorted'
        List<CycleCountItem> cycleCountItems = [
                cycleCountItem1,
                cycleCountItem4,
                cycleCountItem3,
                cycleCountItem0,
                cycleCountItem2,
        ]

        when:
        cycleCountItems.sort()

        then:
        cycleCountItems[0] == cycleCountItem0  // inventoryItem.expirationDate: 2025-01-01, location.name: "C", id: 10
        cycleCountItems[1] == cycleCountItem1  // inventoryItem.expirationDate: 2025-01-01, location.name: "C", id: 11
        cycleCountItems[2] == cycleCountItem2  // inventoryItem.expirationDate: 2025-01-01, location.name: "D", id: 3
        cycleCountItems[3] == cycleCountItem3  // inventoryItem.expirationDate: 2025-01-02, location.name: "A", id: 2
        cycleCountItems[4] == cycleCountItem4  // inventoryItem.expirationDate: 2025-01-02, location.name: "B", id: 1
    }
}
