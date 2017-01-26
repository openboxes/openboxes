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

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import grails.validation.Validateable
import org.pih.warehouse.core.Location

@Validateable(nullable = true)
class TransactionCommand {

	String id
	Date transactionDate
	TransactionType transactionType
	Inventory inventory
	Location location
	Location destination
	Location source
	String comment

	Map productInventoryItems
	Map quantityMap
	List transactionTypeList 
	List locationList
	
	Transaction transaction

	List<TransactionEntryCommand> transactionEntries =
		LazyList.decorate(new ArrayList(),FactoryUtils.instantiateFactory(TransactionEntryCommand.class));
		
		
	static constraints = {
		
	}
	
}
