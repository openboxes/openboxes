package org.pih.warehouse.report

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.product.Product;

class InventoryReportEntryCommand {
	// Entries are index by product or by inventory item
	Product product;
	List<InventoryItem> inventoryItems = []
	Map<InventoryItem, InventoryReportEntryCommand> entries = [:]
	
	// Entries are index by product or by inventory item
	InventoryItem inventoryItem
	List<ProductReportEntryCommand> transactionEntries = []
	
	// Running counts of each aggregate 
	Integer quantityInitial = 0;
	Integer quantityRunning = 0;
	Integer quantityFinal = 0;
	
	Integer quantityTransferredIn = 0;
	Integer quantityFound = 0;
	Integer quantityTotalIn = 0;
	
	Integer quantityTransferredOut = 0;
	Integer quantityConsumed = 0;
	Integer quantityDamaged = 0;
	Integer quantityExpired = 0;
	Integer quantityLost = 0;
	Integer quantityTotalOut = 0;
	
	Integer quantityAdjusted = 0;
		
	Map<Location, Integer> quantityTransferredInByLocation = [:]
	Map<Location, Integer> quantityTransferredOutByLocation = [:]
		
	static constraints = {
	}
	
	
	Integer getQuantityTotalAdjusted() { 
		return quantityFound - quantityLost;
	}
	
	Integer getQuantityEnding() { 
		return quantityTotalIn - quantityTotalOut + getQuantityTotalAdjusted()
	}
	
	
	
	InventoryReportEntryCommand getTotals() { 		
		def totals = new InventoryReportEntryCommand()			
		entries.values().each {
			println "adjusted = " + it.quantityAdjusted
			println "xfer in = " + it.quantityTransferredIn
			println "xfer out = " + it.quantityTransferredOut
			totals.quantityAdjusted += it.quantityAdjusted;
			totals.quantityTransferredIn += it.quantityTransferredIn;
			totals.quantityTransferredOut += it.quantityTransferredOut;
			totals.quantityConsumed += it.quantityConsumed;
			totals.quantityDamaged += it.quantityDamaged
			//entry.quantityEnding += it.quantityEnding;
			totals.quantityExpired += it.quantityExpired;
			totals.quantityFinal += it.quantityFinal;
			totals.quantityFound += it.quantityFound;
			totals.quantityInitial += it.quantityInitial
			totals.quantityLost += it.quantityLost
			totals.quantityRunning += it.quantityRunning
			totals.quantityTotalIn += it.quantityTotalIn
			totals.quantityTotalOut += it.quantityTotalOut				
		}
		return totals;
	}	

	
}