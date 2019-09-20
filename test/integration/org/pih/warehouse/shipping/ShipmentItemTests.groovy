/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.shipping;

import static org.junit.Assert.*
import grails.test.*

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

class ShipmentItemTests extends GroovyTestCase {
	
	def shipmentItem1
	def shipmentItem2
	def shipmentItem3
	def shipmentItem4
	def shipmentItem5
	def shipmentItem6
	
	protected void setUp() {
		super.setUp()
		
		new Category(name: "Medicines").save(flush:true);
		new Product(name: "Ibuprofen", category: Category.findByName("Medicines")).save(flush:true);
		new Product(name: "Tylenol", category: Category.findByName("Medicines")).save(flush:true);
		new ShipmentType(name: "Air").save(flush:true)
		def locationType = new LocationType(name: "Depot").save(flush:true)
		new Location(name: "Boston", locationType: locationType).save(flush:true)
		new Location(name: "Miami", locationType: locationType).save(flush:true)
		new ContainerType(name: "Pallet").save(flush:true);
		new Container(name: "Pallet 1", containerType: ContainerType.findByName("Pallet")).save(flush:true)
		new InventoryItem(lotNumber: "ABC121", expirationDate: new Date()+180).save(flush:true)
		new InventoryItem(lotNumber: "ABC122", expirationDate: new Date()+360).save(flush:true)
		new InventoryItem(lotNumber: "ABC123", expirationDate: new Date()+540).save(flush:true)
		new InventoryItem(lotNumber: "ABC124", expirationDate: new Date()+720).save(flush:true)
		
		def shipment1 = new Shipment();
		shipment1.name = "New Shipment 1"
		shipment1.expectedDeliveryDate = new Date();
		shipment1.expectedShippingDate = new Date();
		shipment1.shipmentType = ShipmentType.findByName("Air");
		shipment1.origin = Location.findByName("Boston")
		shipment1.destination = Location.findByName("Miami")
		shipment1.save(flush:true);
		
		shipmentItem1 = new ShipmentItem()
		shipmentItem1.id = "1"
		shipmentItem1.inventoryItem = InventoryItem.findByLotNumberAndProduct("ABC121", Product.findByName("Ibuprofen"))
		shipmentItem1.quantity = 100
		shipmentItem1.save(flush:true)
		
		shipmentItem2 = new ShipmentItem()
		shipmentItem2.id = "2"
		shipmentItem2.product = Product.findByName("Ibuprofen")
		shipmentItem2.lotNumber = "ABC122"
		shipmentItem2.quantity = 100
		shipmentItem2.save(flush:true)
		
		shipmentItem3 = new ShipmentItem()
		shipmentItem3.id = "3"
		shipmentItem3.product = Product.findByName("Ibuprofen")
		shipmentItem3.lotNumber = "ABC122"
		shipmentItem3.quantity = 50
		shipmentItem3.save(flush:true)
		
		shipmentItem4 = new ShipmentItem()
		shipmentItem4.id = "4"
		shipmentItem4.inventoryItem = InventoryItem.findByLotNumberAndProduct("ABC123", Product.findByName("Ibuprofen"))
		shipmentItem4.quantity = 100
		shipmentItem4.save(flush:true)

		shipmentItem5 = new ShipmentItem()
		shipmentItem5.id = "5"
		shipmentItem5.inventoryItem = InventoryItem.findByLotNumberAndProduct("ABC121", Product.findByName("Ibuprofen"))
		shipmentItem5.quantity = 1
		shipmentItem5.save(flush:true)

		shipmentItem6 = new ShipmentItem()
		shipmentItem6.id = "6"
		shipmentItem6.product = Product.findByName("Ibuprofen")
		shipmentItem6.lotNumber = null
		shipmentItem6.quantity = 1
		shipmentItem6.save(flush:true)

	}


	void test_compareTo_shouldShipmentItem1BeforeShipmentItem2() { 
		def expectedValue = -1
		def actualValue = shipmentItem1.compareTo(shipmentItem2)
		assertEquals expectedValue, actualValue
	}
	
	void test_sort_shouldSortProperly() {
		def expectedValue = [shipmentItem5, shipmentItem1, shipmentItem4, shipmentItem6, shipmentItem3, shipmentItem2]
		def actualValue = [shipmentItem1, shipmentItem2, shipmentItem3, shipmentItem4, shipmentItem5, shipmentItem6]
		actualValue = actualValue.sort()
		println actualValue
		assertEquals expectedValue, actualValue
	}

}

