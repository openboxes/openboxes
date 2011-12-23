package org.pih.warehouse.reporting


import org.pih.warehouse.core.Constants;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import grails.converters.*;

import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.DialogForm;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.UnitOfMeasure;
import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.InventoryService;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.report.InventoryReportCommand;
import org.pih.warehouse.report.InventoryReportEntryCommand;
import org.pih.warehouse.report.ProductReportCommand;
import org.pih.warehouse.report.ProductReportEntryCommand;
import org.pih.warehouse.shipping.Container;
import org.pih.warehouse.shipping.ShipmentItem;
import org.pih.warehouse.shipping.Shipper;
import org.pih.warehouse.shipping.ShipperService;
import org.pih.warehouse.shipping.Shipment;

class ReportController {
	
	def inventoryService
	
	
	def viewProductReport = { ProductReportCommand command -> 	
		
		if (!command?.product) { 
			throw new Exception("Unable to locate product " + params?.product?.id)
		}
		
		command.inventoryItems = InventoryItem.findAllByProduct(command?.product)
		command?.quantityInitial = inventoryService.getQuantity(command?.product, command?.location, command?.startDate)

		def transactionEntries = inventoryService.getTransactionEntries(command?.product, command?.location, command?.startDate, command?.endDate)
				
		def quantity = command?.quantityInitial;
		transactionEntries.each { transactionEntry ->
			def productReportEntry = new ProductReportEntryCommand(transactionEntry: transactionEntry, balance: 0)
			productReportEntry.balance = inventoryService.adjustQuantity(quantity, transactionEntry)
			command.productReportEntryList << productReportEntry
			
			// Need to keep track of the running total so we can adjust the balance as we go
			quantity = productReportEntry.balance
		}
		command.quantityFinal = quantity;
				
		[command : command]
		
		
	}
	
	def viewTransactionReport = { InventoryReportCommand cmd -> 
		
		def transactionEntries = TransactionEntry.list();
		// 
		// so each time you hit an inventory, you compare with the running total, 
		// and add / subract to "adjustment" as appropriate.  then set the running 
		// total to the new inventory and continue with the running total...
		//
		
		//if (cmd.startDate) { 
		//	def initialQuantityMap = inventoryService.getQuantityAsOf(cmd.startDate)
		//}
		
		transactionEntries.each { 			
			def product = it?.inventoryItem?.product
			def transactionType = it?.transaction?.transactionType

			// Filter by category, location, startDate, endDate (should move this to the service layer)			
			if ((!cmd.category || cmd.category == product.category) && 
				(!cmd.location || cmd.location.inventory == it.transaction.inventory) &&
				(!cmd.startDate || it.transaction?.transactionDate?.after(cmd.startDate)) &&
				(!cmd.endDate || it.transaction?.transactionDate?.before(cmd.endDate))) { 
	
				def inventoryReportEntry = cmd.inventoryReportEntryMap[product]
				if (!inventoryReportEntry) { 
					inventoryReportEntry = new InventoryReportEntryCommand(product: product);
					cmd.inventoryReportEntryMap[product] = inventoryReportEntry				
				}
				
				if (transactionType?.id == Constants.CONSUMPTION_TRANSACTION_TYPE_ID) {
					inventoryReportEntry.quantityConsumed += it.quantity
					inventoryReportEntry.quantityTotalOut += it.quantity
				}
				else if (transactionType?.id == Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID) {
					inventoryReportEntry.quantityFound += it.quantity
					inventoryReportEntry.quantityTotalIn += it.quantity

				}
				else if (transactionType?.id == Constants.EXPIRATION_TRANSACTION_TYPE_ID) {
					inventoryReportEntry.quantityExpired += it.quantity				
					inventoryReportEntry.quantityTotalOut += it.quantity
				}
				else if (transactionType?.id == Constants.DAMAGE_TRANSACTION_TYPE_ID) {
					inventoryReportEntry.quantityDamaged += it.quantity
					inventoryReportEntry.quantityTotalOut += it.quantity
				}
				else if (transactionType?.id == Constants.INVENTORY_TRANSACTION_TYPE_ID) {
					
				}
				else if (transactionType?.id == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID) {
					inventoryReportEntry.quantityTransferredIn += it.quantity
					inventoryReportEntry.quantityTotalIn += it.quantity
					
				}
				else if (transactionType?.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
					inventoryReportEntry.quantityTransferredOut += it.quantity				
					inventoryReportEntry.quantityTotalOut += it.quantity
				}
				else if (transactionType?.id == Constants.ADJUSTMENT_DEBIT_TRANSACTION_TYPE_ID) {
					inventoryReportEntry.quantityLost += it.quantity
					inventoryReportEntry.quantityTotalOut += it.quantity
				}
				else if (transactionType?.id == Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID) {
	
				}				
			}
		}
		
		/*
		cmd.products.each { product ->
			def inventoryItems = InventoryItem.findAllByProduct(product)
			inventoryItems.each { inventoryItem ->				
				def transactionEntry = inventoryService.getPreviousInventoryTransactionEntry(inventoryItem, cmd.startDate ?: new Date())
				def lastInventoryDate = transactionEntry?.transaction?.transactionDate
				if (!lastInventoryDate) {
					transactionEntry = inventoryService.getPreviousInventoryTransaction(product, cmd.startDate ?: new Date())
					lastInventoryDate = transactionEntry?.transaction?.transactionDate
				}	
					
				log.info("Last inventory date for " + inventoryItem?.product + " " + inventoryItem?.lotNumber + ": "  + 
					lastInventoryDate)
				
			}
		}
		*/
		
		
		
		[cmd : cmd]
	}
	
}