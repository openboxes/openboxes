package org.pih.warehouse.request

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.codehaus.groovy.grails.validation.Validateable;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;
import org.pih.warehouse.shipping.ShipmentType;

@Validateable
class RequestItemListCommand implements Serializable {
		
	// Not the actual order items, but rather all the line items on the receive order page.  
	// This means that we might have more than one OrderItemCommand per OrderItem.
	def requestItems = 
		LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(RequestItemCommand.class));
	
	static constraints = {
		requestItems(validator: { val, obj, errors ->
			def errorsFound = false;
			val.each{ requestItem ->
				
				// Ignore a null order item
				if (requestItem) { 
					// If the quantity received is not null and the item does not validate, reject the 
					if(requestItem?.quantityReceived && !requestItem?.validate()) {
						requestItem.errors.allErrors.each { error ->
							println(">>>>>>>>>>>>> ERROR " + error.getCode() + " ")
							obj.errors.rejectValue("orderItems", error.getField() + "." + error.getCode())
							
						}
					}
				}
				return errorsFound;
			}
		});		
	}	
}

