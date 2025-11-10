/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package unit.org.pih.warehouse.shipping

import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ShipmentItem
import spock.lang.Specification

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product

class ShipmentItemSpec extends Specification implements DomainUnitTest<ShipmentItem> {

    void 'sort should correctly order the items'() {
        given:
        Container parentContainer1 = new Container(sortOrder: 1)
        Container parentContainer2 = new Container(sortOrder: 2)

        Container container1 = new Container(parentContainer: parentContainer1)
        Container container2 = new Container(parentContainer: parentContainer2, sortOrder: 1)
        Container container3 = new Container(parentContainer: parentContainer2, sortOrder: 2)

        Product product1 = new Product(name: "product1")
        Product product2 = new Product(name: "product2")

        Location bin1 = new Location(name: "bin1")
        Location bin2 = new Location(name: "bin2")

        InventoryItem ii1 = new InventoryItem(product: product1)
        InventoryItem ii2 = new InventoryItem(product: product2, lotNumber: "ii1")
        InventoryItem ii3 = new InventoryItem(product: product2, lotNumber: "ii2")

        ShipmentItem shipmentItem1 = new ShipmentItem(
                container: container1,
        )
        ShipmentItem shipmentItem2 = new ShipmentItem(
                container: container2,
        )
        ShipmentItem shipmentItem3 = new ShipmentItem(
                container: container3,
                inventoryItem: ii1,
                product: ii1.product,
        )
        ShipmentItem shipmentItem4 = new ShipmentItem(
                container: container3,
                inventoryItem: ii2,
                product: ii2.product,
                lotNumber: ii2.lotNumber,
        )
        ShipmentItem shipmentItem5 = new ShipmentItem(
                container: container3,
                inventoryItem: ii3,
                product: ii3.product,
                lotNumber: ii3.lotNumber,
                binLocation: bin1,
        )
        ShipmentItem shipmentItem6 = new ShipmentItem(
                container: container3,
                inventoryItem: ii3,
                product: ii3.product,
                lotNumber: ii3.lotNumber,
                binLocation: bin2,
                quantity: 1,
        )
        ShipmentItem shipmentItem7 = new ShipmentItem(
                container: container3,
                inventoryItem: ii3,
                product: ii3.product,
                lotNumber: ii3.lotNumber,
                binLocation: bin2,
                quantity: 2,
                id: 1,
        )
        ShipmentItem shipmentItem8 = new ShipmentItem(
                container: container3,
                inventoryItem: ii3,
                product: ii3.product,
                lotNumber: ii3.lotNumber,
                binLocation: bin2,
                quantity: 2,
                id: 2,
        )

        List<ShipmentItem> itemsUnsorted = [
                shipmentItem5,
                shipmentItem8,
                shipmentItem1,
                shipmentItem4,
                shipmentItem7,
                shipmentItem6,
                shipmentItem3,
                shipmentItem2,
        ]

        when:
        List<ShipmentItem> itemsSorted = itemsUnsorted.sort()

        then:
        assert itemsSorted.size() == 8
        assert itemsSorted[0] == shipmentItem1
        assert itemsSorted[1] == shipmentItem2
        assert itemsSorted[2] == shipmentItem3
        assert itemsSorted[3] == shipmentItem4
        assert itemsSorted[4] == shipmentItem5
        assert itemsSorted[5] == shipmentItem6
        assert itemsSorted[6] == shipmentItem7
        assert itemsSorted[7] == shipmentItem8
    }
}

