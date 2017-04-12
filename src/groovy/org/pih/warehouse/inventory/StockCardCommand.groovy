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

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment;

class StockCardCommand {
	
	// Used when adding a new inventory item (not implemented yet)
	InventoryItem inventoryItem;
	
	// Entire page
	Product product;
	Location warehouse;
	Inventory inventory;
	InventoryLevel inventoryLevel;
	Integer totalQuantity

	// Current stock section
	List<InventoryItem> lotNumberList
	List<InventoryItem> inventoryItemList;
	List<TransactionEntry> transactionEntryList;
	List<Shipment> pendingShipmentList;
	Map<Transaction, List<TransactionEntry>> transactionEntriesByTransactionMap;
	Map<InventoryItem, List<TransactionEntry>> transactionEntriesByInventoryItemMap
	Map<InventoryItem, Integer> quantityByInventoryItemMap
	List quantityByBinLocation

	Map requisitionMap = [:]
	Map orderMap = [:]
	Map shipmentMap = [:]
	
	// Transaction log section
	Date startDate
	Date endDate
	TransactionType transactionType
	Map transactionLogMap;
	
	static constraints = {
		startDate(nullable:true)
		endDate(nullable:true)
		transactionType(nullable:true)
	}
	
	/**
	 * Return the total quantity for all inventory items.
	 *
	 * @return 	the sum of quantities across all transaction entries
	 */
	//Integer getTotalQuantity() {
	//	return quantityByInventoryItemMap?.values() ? quantityByInventoryItemMap?.values().sum() : 0
	//}
	
	Map getAllTransactionLogMap() { 
		return transactionEntryList.groupBy { it.transaction }
	}
	
	/**
	 * Filter the transaction entry list by date range and transaction type
	 *
	 * TODO Should move this to the DAO/service layer in order to make it perform better.
	 *
	 * @return
	 */
	Map getTransactionLogMap(Boolean enableFilter) {
		
		//println "transaction entries " + transactionEntryList
		def filteredTransactionLog = transactionEntryList;
		/*
		if (enableFilter) { 
			if (startDate) {
				filteredTransactionLog = filteredTransactionLog.findAll{it.transaction.transactionDate >= startDate}
			}
			
			// Need to add +1 to endDate because date comparison includes time
			// TODO Should set endDate to midnight of the date to be more accurate
			if (endDate) {
				filteredTransactionLog = filteredTransactionLog.findAll{it.transaction.transactionDate <= endDate+1}
			}
			
			// Filter by transaction type (0 = return all types)
			if (transactionType && transactionType?.id != 0) {
				filteredTransactionLog = filteredTransactionLog.findAll{it?.transaction?.transactionType?.id == transactionType?.id}
			}
		}
		*/

		return filteredTransactionLog.groupBy { it.transaction }
	}
	
	
	
	
}