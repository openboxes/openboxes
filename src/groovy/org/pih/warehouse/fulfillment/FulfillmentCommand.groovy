package org.pih.warehouse.fulfillment

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.request.Request;
import org.pih.warehouse.request.RequestItem;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;
import org.pih.warehouse.shipping.ShipmentType;

class FulfillmentCommand implements Serializable {

	Request request					// original request
	Fulfillment fulfillment	
	
	def fulfillmentItems =
		LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(FulfillmentItem.class));
	
	static constraints = {
		request(nullable:true)
		fulfillment(nullable:true)		
	}

	
	/**
	 * 
	 * @return
	 */
	Map fulfillmentItemsMap() {
		return fulfillment?.fulfillmentItems?.groupBy { it.requestItem }
	}

   
   /**
    * 
    * @param requestItem
    * @return
    */
   List fulfillmentItems(RequestItem requestItem) { 
	   def fulfillmentItemsMap = fulfillmentItemsMap();
	   if (fulfillmentItemsMap) {
		   return fulfillmentItemsMap.get(requestItem)
	   }
	   return new ArrayList();
   }
	   

	/**
	 * 
	 * @return
	 */
	Map quantityFulfilledMap() {
		Map results = [:]
		Map fulfillmentItemsMap = fulfillmentItemsMap()
		fulfillmentItemsMap.each { requestItem, fulfillItems ->
			def quantity = fulfillItems.sum() { it.quantity }
			results[requestItem] = quantity;
		}
		return results;
	}

	/**
	 * 
	 * @param requestItem
	 * @return
	 */
	Integer quantityFulfilledByRequestItem(RequestItem requestItem) { 
		def quantity = quantityFulfilledMap()[requestItem]
		return quantity ?: 0;
	}

}

