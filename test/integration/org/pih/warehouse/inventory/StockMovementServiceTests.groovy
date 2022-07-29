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
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementDirection
import org.pih.warehouse.requisition.CommodityClass
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentType
import testutils.DbHelper

class StockMovementServiceTests extends GroovyTestCase {

	def stockMovementService
	Requisition requisition
	RequisitionItem requisitionItem1
	RequisitionItem requisitionItem2
	Shipment shipment
	StockMovement stockMovement
	StockMovement stockMovementEmpty

	protected void setUp() {
		super.setUp()

		def origin = DbHelper.findOrCreateLocation('Boston Headquarters')
		assertNotNull(origin)
		def destination = DbHelper.findOrCreateLocation('Miami Warehouse')
		assertNotNull(destination)
		def product1 = DbHelper.findOrCreateProduct('Advil 200mg')
		assertNotNull(product1)
		def product2 = DbHelper.findOrCreateProduct('Tylenol 325mg')
		assertNotNull(product2)
		requisitionItem1 = new RequisitionItem(id: "item1", description: "item1", product: product1, quantity: 10)
		assertNotNull(requisitionItem1)
		requisitionItem2 = new RequisitionItem(id: "item2", description: "item2", product: product2, quantity: 20)
		assertNotNull(requisitionItem2)
		def person = DbHelper.findOrCreateUser('Axl', 'Rose', 'axl@hotmail.com', 'axl', 'Sw337_Ch1ld', false)

		requisition = new Requisition(
				requestNumber: "SM1",
				name: "testRequisition" + UUID.randomUUID().toString()[0..5],
				commodityClass: CommodityClass.MEDICATION,
				type: RequisitionType.DEFAULT,
				origin: origin,
				destination: destination,
				requestedBy: person,
				dateRequested: new Date(),
				requestedDeliveryDate: new Date().plus(1),
				status: RequisitionStatus.CREATED).save(failOnError: true, flush: true)

		shipment = new Shipment(
			destination: destination,
			expectedShippingDate: new Date().plus(1),
			name: 'Speedy Delivery',
			origin: origin,
			shipmentType: new ShipmentType([name: 'hand-deliver']).save()
		).save(failOnError: true, flush: true)

		requisition.addToShipments(shipment)
		requisition.addToRequisitionItems(requisitionItem1)
		requisition.addToRequisitionItems(requisitionItem2)
		requisition.save(failOnError: true, flush: true)

		stockMovement = new StockMovement(
			dateRequested: new Date(),
			destination: destination,
			id: requisition.id,
			origin: origin,
			requestedBy: person,
			requisition: requisition,
			shipment: shipment,
			stockMovementDirection: StockMovementDirection.OUTBOUND
		)
		stockMovementEmpty = new StockMovement(id: "1")
	}

	@Test
	void test_createStockMovement_shouldThrowExceptionOnInvalid() {
	 	shouldFail (ValidationException) {
			stockMovementService.createStockMovement(stockMovementEmpty)
		}
	}

	@Test
	void test_createStockMovement_shouldCreateStockMovement() {
		def sm = stockMovementService.createStockMovement(stockMovement)
		assertNotNull sm
	}

	// FIXME stockMovementService.updateStatus() no longer exists
	@Ignore
	void test_updateStatus_shouldThrowExceptionIfStatusNotInList() {
		shouldFail (IllegalStateException) {
			stockMovementService.updateStatus(requisition.id, RequisitionStatus.ERROR)
		}
	}

	// FIXME stockMovementService.updateStatus() no longer exists
	@Ignore
	void test_updateStatus_shouldUpdateStatus() {

		stockMovementService.updateStatus(requisition.id, RequisitionStatus.CANCELED)
		assert requisition.status == RequisitionStatus.CANCELED
	}

	@Test
	void test_updateRequisition_shouldThrowExceptionIfNoRequisition() {
		shouldFail (ObjectNotFoundException) {
			stockMovementService.updateRequisitionBasedStockMovement(stockMovementEmpty)
		}
	}

	@Test
	void test_updateRequisition_shouldUpdateRequisition() {
		stockMovement.description = "changed"
		def updated = stockMovementService.updateRequisitionBasedStockMovement(stockMovement)
		assert updated.description == "changed"
	}

	@Test
	void test_updateRequisitionWhenShipmentChanged_shouldThrowExceptionIfNoRequisitio() {
		shouldFail (ObjectNotFoundException) {
			stockMovementService.updateRequisitionOnShipmentChange(stockMovementEmpty)
		}
	}

	@Test
	void test_getStockMovements_shouldReturnStockMovements() {
		def maxResults = 2
		def offset = 0
		def stockMovements = stockMovementService.getOutboundStockMovements(maxResults, offset)
		assertNotNull stockMovements
		assert stockMovements.size() == 1
	}

	@Test
	void test_getStockMovement_shouldReturnOneStockMovement() {
		def sm = stockMovementService.getStockMovement(requisition.id)
		assertNotNull sm
		assert sm.name == requisition.name
		assert sm.identifier == "SM1"
		assert sm.statusCode == "CREATED"
	}

	@Test
	void test_getStockMovementItem_shouldReturnStockMovementItem() {
		def item = stockMovementService.getStockMovementItem(requisitionItem1.id)
		assertNotNull item
		assert item.quantityRequested == 10
	}

	@Test
	void test_clearPicklist_shouldClearPicklist() {
		def item = stockMovementService.getStockMovementItem(requisitionItem1.id)
		stockMovementService.clearPicklist(item)
	}
}
