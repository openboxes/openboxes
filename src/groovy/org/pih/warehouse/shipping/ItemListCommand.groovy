package org.pih.warehouse.shipping

import java.io.Serializable;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.codehaus.groovy.grails.validation.Validateable;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Product;

class ItemListCommand implements Serializable {
	
	def items =
		LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(ItemCommand.class));

	static constraints = {
		items(validator: { val, obj, errors ->
			def errorsFound = false;
			/*
			val.each{ item ->
				// Ignore a null order item
				if (item) {
					// If the quantity is not null and the item does not validate, reject the
					if(item?.quantity) {	// && !item?.validate()
						item.errors.allErrors.each { error ->
							println(">>>>>>>>>>>>> ERROR " + error.getCode() + " ")
							obj.errors.rejectValue("orderItems", error.getField() + "." + error.getCode())
						}
					}
				}
			}
			*/
			return errorsFound;
		});
	}
}


