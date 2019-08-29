/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.shipping

import static org.junit.Assert.*

import grails.converters.JSON
import grails.test.*
import grails.validation.ValidationException

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

class ShipmentServiceTests extends GroovyTestCase {

	def shipmentService;
	
	protected void setUp() {
		super.setUp()
		
		new Category(name: "Category").save(flush:true);
		new Product(name: "Product", category: Category.findByName("Category")).save(flush:true);
		new ShipmentType(name: "Ground").save(flush:true)
		def locationType = new LocationType(name: "Depot").save(flush:true)
		new Location(name: "Origin", locationType: locationType).save(flush:true)
		new Location(name: "Destination", locationType: locationType).save(flush:true)
		new ContainerType(name: "Pallet").save(flush:true);
		new ContainerType(name: "Box").save(flush:true);
		
		def shipment1 = new Shipment();
		shipment1.name = "New Shipment 1"
		shipment1.expectedDeliveryDate = new Date();
		shipment1.expectedShippingDate = new Date();
		shipment1.shipmentType = ShipmentType.findByName("Ground");
		shipment1.origin = Location.findByName("Origin")
		shipment1.destination = Location.findByName("Destination")
		shipment1.save(flush:true);
		
		def shipment2 = new Shipment()
		shipment2.name = "New Shipment 2"
		shipment2.expectedDeliveryDate = new Date();
		shipment2.expectedShippingDate = new Date();
		shipment2.shipmentType = ShipmentType.findByName("Ground");
		shipment2.origin = Location.findByName("Origin")
		shipment2.destination = Location.findByName("Destination")
		shipment2.save(flush:true);
		
		def shipmentItem1 = new ShipmentItem()
		shipmentItem1.product = Product.findByName("Product")
		shipmentItem1.inventoryItem = InventoryItem.findByLotNumberAndProduct("ABC123", Product.findByName("Product"))
		shipmentItem1.quantity = 100
		shipmentItem1.save(flush:true)
	}



	void test_saveShipment_shouldSaveShipment() {	
		def shipment3 = new Shipment();
		shipment3.name = "New Shipment 3"
		shipment3.expectedDeliveryDate = new Date();
		shipment3.expectedShippingDate = new Date();
		shipment3.shipmentType = ShipmentType.findByName("Ground");
		shipment3.origin = Location.findByName("Origin")
		shipment3.destination = Location.findByName("Destination")

		shipmentService.saveShipment(shipment3)
		assertNotNull shipment3.id;		
		assertNotNull Shipment.findByName("New Shipment 3");
		
	}
	
	/**
	 * Add a container to a shipment.
	 */
	void test_addToContainers_shouldAddContainerToShipment() { 	
		def shipment = Shipment.findByName("New Shipment 1")
		assertNotNull shipment

		def pallet1 = new Container(name: "Pallet 1", containerType: ContainerType.findByName("Pallet"))
		shipment.addToContainers(pallet1);
		shipment.save(flush:true)	
		
		def shipmentExists = Shipment.findByName("New Shipment 1")
		assertEquals 1, shipmentExists.containers.size()
	}
	
	/**
	 * Add a shipment item to a shipment.
	 */
	void test_addShipmentItems_shouldAddShipmentItemToShipment() {
		def shipment = Shipment.findByName("New Shipment 1")

		def pallet1 = new Container(name: "Pallet 1", containerType: ContainerType.findByName("Pallet"))
		shipment.addToContainers(pallet1);
		shipment.save(flush:true)
		
		def shipmentItem = new ShipmentItem(product: Product.findByName("Product"), quantity: 1, container: pallet1)		
		shipment.addToShipmentItems(shipmentItem)
		shipment.save(flush:true)
		
		def shipmentExists = Shipment.findByName("New Shipment 1")
		assertNotNull shipmentExists
		assertEquals 1, shipmentExists.shipmentItems.size()
	}

	/**
	 * Move a container to another shipment.
	 */
	void test_moveContainer_shouldMoveContainerToShipment() {
		def shipment1 = Shipment.findByName("New Shipment 1")
		def shipment2 = Shipment.findByName("New Shipment 2")
		def pallet1 = new Container(id: "1", name: "Pallet 1", containerType: ContainerType.findByName("Pallet"))
		shipment1.addToContainers(pallet1);
		shipment1.save(flush:true)


        println "shipment2 Containers: " + shipment2.containers

        // Preconditions
		assertNotNull "Shipment should exist", shipment2 
		assertEquals "Should have 1 container", 1, shipment1.containers.size()
		assertEquals "Should have 0 containers",0, shipment2.containers?.size()?:0

		printContainer("Before move", pallet1)
		printShipment("Before move", shipment1)
		printShipment("Before move", shipment2)
		
		// Do the actual move of the container
		shipmentService.moveContainer(pallet1, shipment2)

		printContainer("After move", pallet1)
		printShipment("After move", shipment1)
		printShipment("After move", shipment2)

		assertEquals shipment2, pallet1.shipment
		
		// Postconditions		
		assertEquals "Should have 0 containers", 0, shipment1?.containers?.size()?:0
		assertEquals "Should have 1 containers", 1, shipment2?.containers?.size()?:0	
		assertEquals "Pallet shipment should equal Shipment 2", shipment2, Container.findByName("Pallet 1").shipment
	}

	
	void test_moveContainer_shouldMoveContainerWithShipmentItemsToShipment() { 
		def shipment1 = Shipment.findByName("New Shipment 1")
		def shipment2 = Shipment.findByName("New Shipment 2")
		def pallet1 = new Container(name: "Pallet 1", containerType: ContainerType.findByName("Pallet"))
		shipment1.addToContainers(pallet1);
		shipment1.save(flush:true)

		def shipmentItem = new ShipmentItem(product: Product.findByName("Product"), quantity: 1, container: pallet1)
		shipment1.addToShipmentItems(shipmentItem)
		shipment1.save(flush:true)
		
		assertEquals "Should have 1 shipment items", 1, shipment1?.shipmentItems?.size()?:0
		assertEquals "Should have 0 shipment items", 0, shipment2?.shipmentItems?.size()?:0
		assertEquals "Pallet1 should have 1 shipment items", 1, Container.findByName("Pallet 1")?.shipmentItems?.size()?:0
		assertEquals "Pallet1's shipment should equal Shipment 1", shipment1, Container.findByName("Pallet 1")?.shipment

		printShipment("Before Move", shipment1)
		printShipment("Before Move", shipment2)
		printShipmentItem("Before Move", shipmentItem)

		shipmentService.moveContainer(pallet1, shipment2)

		printShipment("After Move", shipment1)
		printShipment("After Move", shipment2)
		
		// Postconditions
		assertEquals "Should have 0 shipment items", 0, shipment1?.shipmentItems?.size()?:0
		assertEquals "Should have 1 shipment items", 1, shipment2?.shipmentItems?.size()?:0
		assertEquals "Pallet1 should container 1 shipment items", 1, Container.findByName("Pallet 1")?.shipmentItems?.size()?:0
		assertEquals "Pallet shipment should equal Shipment 2", shipment2, Container.findByName("Pallet 1")?.shipment
	}
		
	/**
	* Move a container to another shipment.
	*/
   void test_moveContainer_shouldMoveChildContainersToShipment() {
	   def shipment1 = Shipment.findByName("New Shipment 1")
	   def shipment2 = Shipment.findByName("New Shipment 2")	   
	   def pallet1 = new Container(name: "Pallet 1", containerType: ContainerType.findByName("Pallet"))
	   shipment1.addToContainers(pallet1);
	   shipment1.save(flush:true)

	   def box1 = pallet1.addNewContainer(ContainerType.findByName("Box"))
	   box1.name = "Box 1"
	   pallet1.addToContainers(box1)
	   
	   def shipmentItem1 = new ShipmentItem(product: Product.findByName("Product"), quantity: 1, container: box1)
	   shipment1.addToShipmentItems(shipmentItem1)
	   shipment1.save()
	   	   
	   assertEquals "Box should contain 1 shipment item", 1, box1?.getShipmentItems()?.size()?:0
	   assertEquals "Pallet's shipment should be shipment 1", shipment1, pallet1.shipment
	   assertEquals "Pallet's shipment should contain 1 shipment item ", 1, pallet1.shipment?.shipmentItems?.size()?:0
	   
	   shouldFail (ValidationException) {
		   shipmentService.moveContainer(pallet1, shipment2)
	   }
	   
	   assertEquals "Pallet's shipment should still be shipment 1", shipment1, Container.findByName("Pallet 1").shipment
   }

   /**
    * 
    */
   void test_deleteShipment_shouldCascadeDeleteContainers() {
	   def shipment1 = Shipment.findByName("New Shipment 1")	   
	   def pallet1 = new Container(name: "Pallet 1", containerType: ContainerType.findByName("Pallet"))	   
	   def shipmentItem1 = new ShipmentItem(product: Product.findByName("Product"), quantity: 1, container: pallet1)
	   shipment1.addToShipmentItems(shipmentItem1)
	   shipment1.addToContainers(pallet1);
	   shipment1.save(flush:true)

       def shipmentId = shipment1.id
	   assertEquals 1, shipment1.shipmentItems.size()
	   assertEquals 1, shipment1.containers.size()
	   def numberOfShipmentsBeforeDelete = Shipment.count()
	   def numberOfContainersBeforeDelete = Container.count()
       def shipmentToDelete = Shipment.get(shipmentId)
       assertNotNull shipmentToDelete
       shipmentService.deleteShipment(shipment1)

       def deletedShipment = Shipment.get(shipmentId)
       assertNull "Should be null", deletedShipment
	   assertEquals "Should be equal to # of shipments before delete minus 1", numberOfShipmentsBeforeDelete-1, Shipment.count()
	   assertEquals "Should be equal to # of containers before delete minus 1", numberOfContainersBeforeDelete-1, Container.count()
	   
   }
   
   void test_deleteShipment_shouldCascadeDeleteShipmentItems() {
	   def shipment1 = Shipment.findByName("New Shipment 1")
	   def pallet1 = new Container(name: "Pallet 1", containerType: ContainerType.findByName("Pallet"))
	   def shipmentItem1 = new ShipmentItem(product: Product.findByName("Product"), quantity: 1, container: pallet1)
	   shipment1.addToShipmentItems(shipmentItem1)
	   shipment1.addToContainers(pallet1);
	   shipment1.save(flush:true)

	   def testShipment = Shipment.findByName("New Shipment 1")
	   assertEquals 1, testShipment.shipmentItems.size()
	   assertEquals 1, testShipment.containers.size()
	   assertEquals 1, testShipment.containers.toArray()[0].shipmentItems.size()
	   
	   def numberOfShipmentItemsBeforeDelete = ShipmentItem.count()
	   def numberOfShipmentsBeforeDelete = Shipment.count()
	   
	   shipmentService.deleteShipment(testShipment)
	   
	   assertEquals numberOfShipmentsBeforeDelete-1, Shipment.count()
	   assertEquals numberOfShipmentItemsBeforeDelete-1, ShipmentItem.count()
	   
   }

   /**
    * @TODO Should be moved to  
    */
   void test_deleteContainer_shouldDeleteContainerButNotShipmentItems() {
	   def shipment1 = Shipment.findByName("New Shipment 1")
	   def pallet1 = new Container(name: "Pallet 1", containerType: ContainerType.findByName("Pallet"))
	   def shipmentItem1 = new ShipmentItem(product: Product.findByName("Product"), quantity: 1, container: pallet1)
	   shipment1.addToShipmentItems(shipmentItem1)
	   shipment1.addToContainers(pallet1);
	   shipment1.save(flush:true)

	   def shipmentBefore = Shipment.findByName("New Shipment 1")
	   assertEquals 1, shipmentBefore.shipmentItems.size()
	   assertEquals 1, shipmentBefore.containers.size()
	   assertEquals 1, shipmentBefore.containers.toArray()[0].shipmentItems.size()
	   def numberOfShipmentItemsBeforeDelete = ShipmentItem.count()
	   def numberOfContainersBeforeDelete = Container.count()
	    
	   shipmentService.deleteContainer(pallet1)
	   	   
	   def shipmentAfter = Shipment.findByName("New Shipment 1")
	   assertEquals 0, shipmentAfter.shipmentItems.size()
	   assertEquals "Should be equal to # of containers before delete minus 1", numberOfContainersBeforeDelete-1, Container.count()
	   
   }

   void test_deleteShipmentItem_shouldDeleteShipmentItemFromShipment() {
	   def shipment = Shipment.findByName("New Shipment 1")
	   def shipmentItem1 = new ShipmentItem(product: Product.findByName("Product"), quantity: 1)
	   shipment.addToShipmentItems(shipmentItem1)	      
	   assertEquals "Should be equal to 1", 1, shipment?.shipmentItems?.size()?:0
	   shipmentService.deleteShipmentItem(shipmentItem1)	   
	   assertEquals "Should be equal to 0", 0, shipment?.shipmentItems?.size()?:0
   }
   
   
   void test_deleteShipmentItem_shouldDeleteShipmentItemWithContainerFromShipment() {
	   def shipment = Shipment.findByName("New Shipment 1")
	   def pallet1 = new Container(name: "Pallet 1", containerType: ContainerType.findByName("Pallet"))
	   shipment.addToContainers(pallet1)
	   shipment.save(flush:true)
	   
	   def shipmentItem1 = new ShipmentItem(product: Product.findByName("Product"), quantity: 1, container: pallet1)
	   shipment.addToShipmentItems(shipmentItem1)
	   shipment.save(flush:true)
	   
	   def shipmentItemCount = ShipmentItem.count()
	   assertEquals "Should be equal to 1", 1, shipment?.shipmentItems?.size()?:0
	   shipmentService.deleteShipmentItem(shipmentItem1)
	   assertEquals "Should be equal to 0", 0, shipment?.shipmentItems?.size()?:0
	   assertEquals "Should be equal to 0", 0, Container.findByName("Pallet 1")?.getShipmentItems()?.size()?:0
	   assertEquals "Should be equal to 1", shipmentItemCount-1, ShipmentItem.count()
   }
   	
   void printShipment(text, shipment) { 
	   println "===================== ${text} :: ${shipment.name} [${shipment.class.name}] ====================="
	   println shipment as JSON
	   shipment.shipmentItems.each { 
		   printShipmentItem(text, it)
	   }
	   println "---------------------------------------------------"
   }
   
   void printContainer(text, container) {
	   println "----------------- ${text} :: ${container.name} [${container.class.name}] --------------------"
	   println container as JSON	   
	   println "---------------------------------------------------"
   }
   
   void printShipmentItem(text, shipmentItem) {
	   println "----------------- ${text} :: ${shipmentItem.id} [${shipmentItem.class.name}] --------------------"
	   println shipmentItem as JSON
	   println "---------------------------------------------------"
   }
}

