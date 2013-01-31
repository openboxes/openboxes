/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/ 
package org.pih.warehouse.requisition

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.LocationType;
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.product.Product

class RequisitionService {

	boolean transactional = true

	def shipmentService;
	def inventoryService;



	Requisition saveRequisition(Map data, Location userLocation) {

		def itemsData = data.requisitionItems ?: []
		data.remove("requisitionItems")

		def requisition = Requisition.get(data.id?.toString()) ?: new Requisition(status: RequisitionStatus.CREATED)
		requisition.properties = data

		def requisitionItems = itemsData.collect{  itemData ->
			def requisitionItem = requisition.requisitionItems?.find{i -> itemData.id  && i.id == itemData.id }
			if(requisitionItem) {
				requisitionItem.properties = itemData
			}
			else{
				requisitionItem = new RequisitionItem(itemData)
				requisition.addToRequisitionItems(requisitionItem)
			}
			requisitionItem
		}

		def itemsToDelete = requisition.requisitionItems.findAll { dbItem ->
			!requisitionItems.any{ clientItem-> clientItem.id == dbItem.id}
		}
		itemsToDelete.each{requisition.removeFromRequisitionItems(it)}
		requisition.destination = userLocation
		requisition.save(flush:true)
		requisition.requisitionItems?.each{it.save(flush:true)}
		requisition
	}

	void deleteRequisition(Requisition requisition) {
		requisition.delete(flush: true)
	}

	void cancelRequisition(Requisition requisition) {
		requisition.status = RequisitionStatus.CANCELED
		requisition.save(flush: true)
	}
}
