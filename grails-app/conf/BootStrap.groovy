import java.util.ArrayList;
import java.util.Date;
import org.pih.warehouse.Attachment;
import org.pih.warehouse.Country;
import org.pih.warehouse.Inventory;
import org.pih.warehouse.InventoryLineItem;
import org.pih.warehouse.Product;
import org.pih.warehouse.StockCard;
import org.pih.warehouse.Shipment;
import org.pih.warehouse.ShipmentLineItem;
import org.pih.warehouse.ShipmentMethod;
import org.pih.warehouse.ShipmentStatus;
import org.pih.warehouse.Transaction;
import org.pih.warehouse.TransactionEntry;
import org.pih.warehouse.TransactionType;
import org.pih.warehouse.User;
import org.pih.warehouse.Warehouse;

class BootStrap {
	
	def init = { servletContext ->
		
		/* Shipment Method */
	
	 	ShipmentMethod fedex = new ShipmentMethod(
	 		
 			name:"FedEx",
 			methodName:"fedex",
 			trackingNumberFormat:"999999999999", 
 			//trackingUrl:"http://www.google.com/search?hl=en&site=&q=",
 			trackingUrl:"http://www.fedex.com/Tracking?ascend_header=1&clienttype=dotcom&cntry_code=us&language=english&tracknumbers=%s",
 			trackingUrlParameterName:""
	 	).save();
	 	ShipmentMethod ups = new ShipmentMethod(
	 		name:"UPS", 
 			methodName:"ups",
 			trackingNumberFormat:"1Z9999W99999999999", 
 			//trackingUrl:"http://www.google.com/search?hl=en&site=&q=",
	 		trackingUrl:"http://wwwapps.ups.com/WebTracking/processInputRequest?sort_by=status&tracknums_displayed=1&TypeOfInquiryNumber=T&loc=en_US&InquiryNumber1=%s&track.x=0&track.y=0",
 			trackingUrlParameterName:""
	 	).save();
	 	ShipmentMethod usps = new ShipmentMethod(
	 		name:"US Postal Service", 
 			methodName:"usps",
 			trackingNumberFormat:"", 
	 		trackingUrl:"", 
	 		trackingUrlParameterName:""
	 	).save();
	 	ShipmentMethod courier = new ShipmentMethod(
	 		name:"Courier",
	 		methodName:"courier"
	 	).save();
	 	
	 	/*
	 	ProductLookup product = new ProductLookup(
	 		methodName:"UPC Lookup",
	 		trackingUrl:"http://www.upcdatabase.com/item/${product.ean}"	 		
	 	)*/
	 	
	 	
		/* Shipment Status */
	 	
	 	ShipmentStatus notShipped = new ShipmentStatus(name:"Not Yet Shipped", finalStatus:false).save();
	 	ShipmentStatus inTransit = new ShipmentStatus(name:"In Transit", finalStatus:false).save();
	 	ShipmentStatus inCustoms = new ShipmentStatus(name:"In Customs", finalStatus:false).save();
	 	ShipmentStatus completed = new ShipmentStatus(name:"Delivered", finalStatus:true).save();	
	
		/* Countries */
	 	
		new Country(country: "Canada", population: 24251210, gdp: 24251210, date: new Date()).save();
		new Country(country: "Haiti", population: 29824821, gdp: 24251210, date: new Date()).save();
		new Country(country: "Mexico", population: 103593973, gdp: 24251210, date: new Date()).save();
		new Country(country: "United States", population: 300000000, gdp: 24251210, date: new Date()).save();
		
		
		/* Users */
		
		User user1 = new User(
			email:"jmiranda@pih.org", 
			firstName:"Justin", 
			lastName:"Miranda",
			role:"Stock Manager", 
			username:"jmiranda", 
			password: "password"
		).save();

		User user2 = new User(
			email:"manager@pih.org", 
			firstName:"Mister", 
			lastName:"Manager",
			role:"Stock Manager", 
			username:"manager", 
			password: "password"
		).save();
		
		
		/* Products */
		
		Product product1 = new Product(
			ean:"073333531084",
			productCode:"1",
			name: "Advil 200mg",
			description: "Ibuprofen 200 mg",
			category: "Pain Reliever",
			user: user1,
			stockCard: new StockCard()
		).save();
		
		Product product2 = new Product(
			ean:"073333531084",
			productCode:"2",
			name: "Tylenol 325mg",
			description: "Acetominophen 325 mg",
			category: "Pain Reliever",
			user: user1,
			stockCard: new StockCard()
		).save();
		
		Product product3 = new Product(
			ean:"073333531084",
			productCode:"3",
			name: "Asprin 20mg",
			description: "Asprin 20mg",
			category: "Pain Reliever",
			user: user1,
			stockCard: new StockCard()
		).save(flush:true);
		
		Product product4 = new Product(
			ean:"073333531084",
			productCode:"4",
			name: "Test Product",
			description: "",
			category: "Unknown",
			user: user1,
			stockCard: new StockCard()				
		).save(flush:true)
		
		
		
		
		/* Warehouses */
		
		Warehouse warehouse1 = new Warehouse(
			name: "Boston Headquarters",
			city: "Boston, MA",
			country: "United States",
			manager: user1
		).save(flush:true);
		
		Warehouse warehouse2 = new Warehouse(
			name: "Miami Warehouse",
			city: "Miami, FL",
			country: "United States",
			manager: user1
		).save(flush:true);
		
		Warehouse warehouse3 = new Warehouse(
			name: "Tabarre Depot",
			city: "Tabarre",
			country: "Haiti",
			manager: user1
		).save(flush:true);
		
		
		/* Warehouse > Inventory > Inventory items */
		
		// Create new inventory
		Inventory inventory1 = new Inventory(
			warehouse:warehouse3,
			lastInventoryDate: new Date()
		).save(flush:true);
		
		// Create new inventory item
		InventoryLineItem inventoryItem1 = new InventoryLineItem(
			product: product1,	    
			quantity: 100,
			reorderQuantity: 50,
			idealQuantity: 100,
			binLocation: "Warehouse Bin A1"
		).save(flush:true);
		inventory1.addToInventoryLineItems(inventoryItem1).save(flush:true, validate:false);
		
		/* Warehouse > Transactions > Transaction Entries */
		
		TransactionType transactionType1 = new TransactionType(
				name:"Incoming Shipment"
		).save(flush:true, validate:true);
		
		Transaction transaction1 = new Transaction(
			transactionDate:new Date(),
			//localWarehouse:warehouse2,
			targetWarehouse:warehouse1,
			transactionType:transactionType1
		); // removed .save(flush:true);
		warehouse3.addToTransactions(transaction1).save();
		
		TransactionEntry transactionEntry1 = new TransactionEntry(
			product:product1,
			quantityChange:50,
			confirmDate:new Date()
		);
		transaction1.addToTransactionEntries(transactionEntry1).save(flush:true, validate:false);
		
		
		/* Create a new shipment */
		
		Shipment shipment1 = new Shipment(			
			delivered: Boolean.FALSE,
			status: "An order for supplies has been received", 
			comment: "ship me as soon as possible",
			shippingMethod: ups,
			trackingNumber: "1Z9999W99999999999",
			expectedShippingDate : Date.parse("yyyy-MM-dd", "2010-06-01"),
			expectedDeliveryDate : Date.parse("yyyy-MM-dd", "2010-06-06"),
			source : warehouse1,
			target : warehouse2
		).save(flush:true);	
		/*
		Attachment document1 = new Attachment(
			filename: "shipment-packing-list.pdf", 
			type: "Packing List", 
			size: 1020L,
			contents: "empty"
		)
		shipment1.addToDocuments(document1).save(flush:true);		
		Attachment document2 = new Attachment(
			filename: "shipment-invoice.pdf", 
			type: "Invoice", 
			size: 990L,
			contents: "empty"
		) 
		shipment1.addToDocuments(document2).save(flush:true);
		*/
		ShipmentLineItem shipmentLineItem1 = new ShipmentLineItem(
			product : product1,
			quantity : 100			
		);
		shipment1.addToShipmentLineItems(shipmentLineItem1).save(flush:true);
		ShipmentLineItem shipmentLineItem2 = new ShipmentLineItem(
			product : product2,
			quantity : 200			
		);
		shipment1.addToShipmentLineItems(shipmentLineItem2).save(flush:true);
		ShipmentLineItem shipmentLineItem3 = new ShipmentLineItem(
			product : product3,
			quantity : 300			
		);
		shipment1.addToShipmentLineItems(shipmentLineItem3).save(flush:true);
		
		def destroy = {
		
		}
		
	}
	
}
