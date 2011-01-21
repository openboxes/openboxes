package org.pih.warehouse.product

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;

class BatchEditCommand {
	
	Category rootCategory;
	List productInstanceList =
		LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Product.class));
	List categoryInstanceList =
		LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Category.class));
	
	static constraints = {
		rootCategory(nullable:true)
	}
	
}

