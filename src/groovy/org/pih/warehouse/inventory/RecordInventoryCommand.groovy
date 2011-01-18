package org.pih.warehouse.inventory

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.product.Product;
import org.codehaus.groovy.grails.validation.Validateable


class RecordInventoryCommand {
	
	Product product;
	Date transactionDate = new Date();
	Inventory inventory;
	RecordInventoryRowCommand recordInventoryRow;
	List<RecordInventoryRowCommand> recordInventoryRows =
		LazyList.decorate(new ArrayList(),FactoryUtils.instantiateFactory(RecordInventoryRowCommand.class));
		//ListUtils.lazyList([], FactoryUtils.constantFactory(new RecordInventoryRowCommand())) 
		// new ListUtils.lazyList(new ArrayList(),{new RecordInventoryRowCommand()} as Factory)
	
	static constraints = {
		inventory(nullable:false) 
		product(nullable:false)
		transactionDate(nullable:false)		
		recordInventoryRows(validator: { val, obj, errors -> 
			def errorsFound = false;
			val.each{ row ->
				println "validate row " + row	
				if(!row.validate()) {
					errorsFound = true;
					row.errors.allErrors.each{ error->
						obj.errors.rejectValue('recordInventoryRows', "recordInventoryCommand.recordInventoryRows.invalid",
							[row, error.getField(), error.getRejectedValue()] as Object[],
							"Property [${error.getField()}] of lot number #${row?.lotNumber} with value [${error.getRejectedValue()}] is invalid.")
					}
				}
				return errorsFound;
			}
		});
	}
}


