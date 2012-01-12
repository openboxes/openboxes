package org.pih.warehouse.report

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.shipping.Shipment;

class ChecklistReportCommand {

	Shipment shipment;
	Location location;
	Date date;
	Date startDate;
	Date endDate;	
	Category category;
	Category rootCategory;

	List<Shipment> shipments;
	List<ChecklistReportEntryCommand> checklistReportEntryList = []	
	Map<Product, ChecklistReportEntryCommand> checklistReportEntryMap = [:]

	static constraints = {
		shipment(nullable:false)
		location(nullable:true)
		startDate(nullable:true)
		endDate(nullable:true)
		category(nullable:true)
	}
	
}