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
        mockDomain(InventoryItem)
        mockDomain(Location)
        CycleCountItem cycleCountItem1 = new CycleCountItem(
                inventoryItem: new InventoryItem(),
                location: new Location()
        )
        CycleCountItem cycleCountItem2 = new CycleCountItem(
                inventoryItem: new InventoryItem(),
                location: new Location()
        )
        CycleCountItem cycleCountItem3 = new CycleCountItem(
                inventoryItem: new InventoryItem(),
                location: new Location()
        )
        CycleCountItem cycleCountItem4 = new CycleCountItem(
                inventoryItem: new InventoryItem(),
                location: new Location()
        )
        CycleCountItem cycleCountItem5 = new CycleCountItem(
                inventoryItem: new InventoryItem(),
                location: new Location()
        )

        and:
        cycleCountItem1.inventoryItem.expirationDate = new Date(2025, 03, 02)
        cycleCountItem2.inventoryItem.expirationDate = new Date(2025, 02, 02)
        cycleCountItem3.inventoryItem.expirationDate = new Date(2025, 02, 02)
        cycleCountItem4.inventoryItem.expirationDate = new Date(2025, 01, 02)
        cycleCountItem5.inventoryItem.expirationDate = new Date(2025, 01, 02)
        cycleCountItem2.location.name = 'A'
        cycleCountItem3.location.name = 'B'
        cycleCountItem4.location.name = 'C'
        cycleCountItem5.location.name = 'C'
        List<CycleCountItem> cycleCountItems = new ArrayList<>()
        cycleCountItems.addAll(Arrays.asList(cycleCountItem5, cycleCountItem4, cycleCountItem3, cycleCountItem2, cycleCountItem1));
        when:
        cycleCountItems.forEach { println(it.hashCode()) }
        println("====")
        cycleCountItems.sort()
        cycleCountItems.forEach { println(it.hashCode()) }

        then:
        cycleCountItems[0] == cycleCountItem4
        cycleCountItems[1] == cycleCountItem5
        cycleCountItems[2] == cycleCountItem2
        cycleCountItems[3] == cycleCountItem3
        cycleCountItems[4] == cycleCountItem1
    }
}
