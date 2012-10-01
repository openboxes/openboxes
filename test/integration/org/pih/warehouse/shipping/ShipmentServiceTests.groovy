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

import static org.junit.Assert.*;

import grails.test.*
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.LocationType;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;

class ShipmentServiceTests extends GroovyTestCase {

	def shipmentService;
	
	protected void setUp() {
		super.setUp()
		
		new Category(name: "Category").save();
		new Product(name: "Product", category: Category.findByName("Category")).save();
		new ShipmentType(name: "Ground").save()
		def locationType = new LocationType(name: "Depot").save()
		new Location(name: "Origin", locationType: locationType).save()
		new Location(name: "Destination", locationType: locationType).save()
		new ContainerType(name: "Pallet").save();
		new ContainerType(name: "Box").save();
		
		def shipment1 = new Shipment();
		shipment1.name = "New Shipment 1"
		shipment1.expectedDeliveryDate = new Date();
		shipment1.expectedShippingDate = new Date();
		shipment1.shipmentType = ShipmentType.findByName("Ground");
		shipment1.origin = Location.findByName("Origin")
		shipment1.destination = Location.findByName("Destination")
		shipment1.save();
		
		def shipment2 = new Shipment()
		shipment2.name = "New Shipment 2"
		shipment2.expectedDeliveryDate = new Date();
		shipment2.expectedShippingDate = new Date();
		shipment2.shipmentType = ShipmentType.findByName("Ground");
		shipment2.origin = Location.findByName("Origin")
		shipment2.destination = Location.findByName("Destination")
		shipment2.save();

		
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testSaveShipment() {	
		def shipment3 = new Shipment();
		shipment3.name = "New Shipment 3"
		shipment3.expectedDeliveryDate = new Date();
		shipment3.expectedShippingDate = new Date();
		shipment3.shipmentType = ShipmentType.findByName("Ground");
		shipment3.origin = Location.findByName("Origin")
		shipment3.destination = Location.findByName("Destination")
		
		shipment3.save(failOnError: true)
		//shipmentService.saveShipment(shipment)
		assertNotNull shipment3.id;
		
		assertNotNull Shipment.findByName("New Shipment 3");
		
	}
	
	/**
	 * Add a container to a shipment.
	 */
	void testAddContainersToShipment() { 	
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
	void testAddShipmentItemsToShipment() {
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
	void testMoveContainerToShipment() {
		def shipment1 = Shipment.findByName("New Shipment 1")
		
		def pallet1 = new Container(name: "Pallet 1", containerType: ContainerType.findByName("Pallet"))
		shipment1.addToContainers(pallet1);
		shipment1.save(flush:true)

		def shipment2 = Shipment.findByName("New Shipment 2")				
		assertNotNull shipment2 
		assertEquals 1, shipment1.containers.size()
		assertNull shipment2.containers		
		
		shipmentService.moveContainer(pallet1, shipment2)

		def testShipment2 = Shipment.findByName("New Shipment 2")
		assertEquals 1, testShipment2.containers.size()		

		def testPallet = Container.findByName("Pallet 1")
		assertEquals testShipment2, testPallet.shipment
				
		// Assertion fails because the moveContainer method simply changes the 
		// ShipmentItem.container and Container.parentContainer references 
		def testShipment1 = Shipment.findByName("New Shipment 1")
		assertEquals 0, testShipment1.containers.size()
		
	}
	
	/**
	* Move a container to another shipment.
	*/
   void testMoveContainerWithContainersToShipment() {
	   def shipment1 = Shipment.findByName("New Shipment 1")
	   
	   def pallet1 = new Container(name: "Pallet 1", containerType: ContainerType.findByName("Pallet"))
	   shipment1.addToContainers(pallet1);
	   shipment1.save(flush:true)

	   def box1 = pallet1.addNewContainer(ContainerType.findByName("Box"))
	   box1.name = "Box 1"
	   pallet1.addToContainers(box1)	   
	   shipmentService.saveContainer(pallet1)
	   
	   
	   def shipmentItem1 = new ShipmentItem(product: Product.findByName("Product"), quantity: 1, container: box1)
	   shipment1.addToShipmentItems(shipmentItem1).save()
	   	   
	   def shipment2 = Shipment.findByName("New Shipment 2")	   
	   shipmentService.moveContainer(pallet1, shipment2)
	   
	   def testPallet = Container.findByName("Pallet 1")	   
	   assertEquals testPallet.shipment, shipment2
	   assertEquals testPallet.containers.size(), 1
	   assertNotNull testPallet.containers.find { it.name == "Box 1" }
	   assertEquals testPallet.shipment.shipmentItems.size(), 1
	  
	   def testBox = Container.findByName("Box 1")
	   assertNotNull testBox
	   assertEquals testBox.shipmentItems.size(), 1
   }

   /**
    * 
    */
   void testCascadeDeleteContainer() {
	   def shipment1 = Shipment.findByName("New Shipment 1")	   
	   def pallet1 = new Container(name: "Pallet 1", containerType: ContainerType.findByName("Pallet"))	   
	   def shipmentItem1 = new ShipmentItem(product: Product.findByName("Product"), quantity: 1, container: pallet1)
	   //pallet1.addToShipmentItems(shipmentItem1)
	   shipment1.addToShipmentItems(shipmentItem1)
	   shipment1.addToContainers(pallet1);
	   shipment1.save(flush:true)

	   def testShipment = Shipment.findByName("New Shipment 1")
	   assertEquals 1, testShipment.shipmentItems.size()
	   assertEquals 1, testShipment.containers.size()
	   assertEquals 1, testShipment.containers.toArray()[0].shipmentItems
	     
	   
	   
	   
   }
	
}

