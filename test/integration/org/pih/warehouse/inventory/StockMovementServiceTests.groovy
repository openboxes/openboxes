/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.inventory

import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.CommodityClass
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType

import static org.junit.Assert.assertNotNull

class StockMovementServiceTests extends GroovyTestCase {

	def stockMovementService
	def requisition
	StockMovement stockMovement
	StockMovement stockMovementEmpty
	
	protected void setUp() {
		super.setUp()

		def location = Location.list().first()
		def product1 = Product.findByName("Advil 200mg")
		def product2 = Product.findByName("Tylenol 325mg")
		def item1 = new RequisitionItem(id: "item1", description: "item1", product: product1, quantity: 10)
		def item2 = new RequisitionItem(id: "item2", description: "item2", product: product2, quantity: 20)
		def person = Person.list().first()
		def requisition = new Requisition(
				id: "requisitionID",
				name: "testRequisition"+ UUID.randomUUID().toString()[0..5],
				commodityClass: CommodityClass.MEDICATION,
				type:  RequisitionType.NON_STOCK,
				origin: location,
				destination: location,
				requestedBy: person,
				dateRequested: new Date(),
				requestedDeliveryDate: new Date().plus(1),
				status: RequisitionStatus.VERIFYING)

		requisition.addToRequisitionItems(item1)
		requisition.addToRequisitionItems(item2)
		requisition.save(flush:true)

		stockMovement = new StockMovement(id: "requisitionID", name: "TestSM", identifier: "SM1", statusCode: "SM2")
		stockMovementEmpty = new StockMovement(id: "1")
	}

	void test_createStockMovement_shouldThrowExceptionOnInvalid() {
	 	shouldFail (ValidationException) {
			stockMovementService.createStockMovement(stockMovementEmpty)
		}
	}

	void test_createStockMovement_shouldCreateStockMovement() {
		def sm = stockMovementService.createStockMovement(stockMovement)
		assertNotNull sm
	}

	void test_updateStatus_shouldThrowExceptionIfStatusNotInList() {
		shouldFail (IllegalStateException) {
			stockMovementService.updateStatus("requisitionID", RequisitionStatus.ERROR)
		}
	}

	void test_updateStatus_shouldUpdateStatus() {
		stockMovementService.updateStatus("requisitionID", RequisitionStatus.CANCELED)
		assert requisition.status == RequisitionStatus.CANCELED
	}

	void test_updateRequisition_shouldThrowExceptionIfNoRequisition() {
		shouldFail (ObjectNotFoundException) {
			stockMovementService.updateRequisition(stockMovementEmpty)
		}
	}

	void test_updateRequisition_shouldUpdateRequisition() {
		stockMovement.description = "changed"
		def updated = stockMovementService.updateRequisition(stockMovement)
		assert updated.description == "changed"
	}

	void test_updateRequisitionWhenShipmentChanged_shouldThrowExceptionIfNoRequisitio() {
		shouldFail (ObjectNotFoundException) {
			stockMovementService.updateRequisitionWhenShipmentChanged(stockMovementEmpty)
		}
	}

	void test_getStockMovements_shouldReturnStockMovements() {
		def maxResults = 2
		def offset = 0
		def stockMovements = stockMovementService.getStockMovements(maxResults, offset)
		assertNotNull stockMovements
		assert stockMovements.size() == 1
	}

	void test_getStockMovement_shouldReturnOneStockMovement() {
		def sm = stockMovementService.getStockMovement("requisitionID")
		assertNotNull sm
		assert sm.name == "TestSM"
		assert sm.identifier == "SM1"
		assert sm.statusCode == "SM2"
	}

	void test_getStockMovementItem_shouldReturnStockMovementItem() {
		def item = stockMovementService.getStockMovementItem("item1")
		assertNotNull item
		assert item.quantityRequested == 10
	}

	void test_clearPicklist_shouldClearPicklist() {
		def item = stockMovementService.getStockMovementItem("item1")
		stockMovementService.clearPicklist(item)
	}

	void test_getEditPage_shouldGetEditPage() {
		def editPage = stockMovementService.getEditPage("requisitionID")
		assertNotNull editPage
		assertNotNull editPage.editPageItems.size() == 2
	}

	void test_getPickPage_shouldGetPickPage() {
		def pickPage = stockMovementService.getPickPage("requisitionID")
		assertNotNull pickPage
		assertNotNull pickPage.pickPageItems.size() == 2
	}

	void test_getPackPage_shouldGetPackPage() {
		def packPage = stockMovementService.getPackPage("requisitionID")
		assertNotNull packPage
		assertNotNull packPage.packPageItems.size() == 2
	}
}

