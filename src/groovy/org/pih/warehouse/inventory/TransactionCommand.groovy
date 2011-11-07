package org.pih.warehouse.inventory

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.codehaus.groovy.grails.validation.Validateable;

@Validateable
class TransactionCommand {
	
	Date transactionDate
	TransactionType transactionType
	
	Map productInventoryItems
	Map quantityMap
	List transactionTypeList 
	List locationList
	
	Transaction transactionInstance
	Warehouse warehouseInstance
	
	List<TransactionEntryCommand> transactionEntries =
		LazyList.decorate(new ArrayList(),FactoryUtils.instantiateFactory(TransactionEntryCommand.class));
		
		
	static constraints = {
		
	}
	
}
