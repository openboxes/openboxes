import java.util.ArrayList;
import java.util.Date;
import org.pih.warehouse.Address;
import org.pih.warehouse.Document;
import org.pih.warehouse.Category;
import org.pih.warehouse.Country;
import org.pih.warehouse.Container;
import org.pih.warehouse.ConditionType;
import org.pih.warehouse.ContainerType;
import org.pih.warehouse.EventType;
import org.pih.warehouse.Inventory;
import org.pih.warehouse.InventoryItem;
import org.pih.warehouse.Organization;
import org.pih.warehouse.DrugProduct;
import org.pih.warehouse.Product;
import org.pih.warehouse.ProductType;
import org.pih.warehouse.ReferenceType;
import org.pih.warehouse.ReferenceNumber;
import org.pih.warehouse.StockCard;
import org.pih.warehouse.Shipment;
import org.pih.warehouse.ShipmentEvent;
import org.pih.warehouse.ShipmentItem;
import org.pih.warehouse.ShipmentMethod;
import org.pih.warehouse.ShipmentStatus;
import org.pih.warehouse.Transaction;
import org.pih.warehouse.TransactionEntry;
import org.pih.warehouse.TransactionType;
import org.pih.warehouse.User;
import org.pih.warehouse.Warehouse;

class BootStrap {
	
	def init = { servletContext ->

	 	/* Categories */
	 	Category ROOT_CATEGORY = new Category(parent: null, name: "Root category").save();
		Category SUBCATEGORY1 = new Category(parent: ROOT_CATEGORY, name: "Sub category 1").save();
		Category SUBCATEGORY2 = new Category(parent: ROOT_CATEGORY, name: "Sub category 2").save();
		Category SUBCATEGORY3 = new Category(parent: ROOT_CATEGORY, name: "Sub category 3").save();
	
		/* Countries */	 	
		Country CANADA = new Country(country: "Canada", population: 24251210, gdp: 24251210, date: new Date()).save();
		Country HAITI = new Country(country: "Haiti", population: 29824821, gdp: 24251210, date: new Date()).save();
		Country MEXICO = new Country(country: "Mexico", population: 103593973, gdp: 24251210, date: new Date()).save();
		Country USA = new Country(country: "United States", population: 300000000, gdp: 24251210, date: new Date()).save();
	
		/* Organizations */
		Organization ZL = new Organization(name:  "Zanmi Lasante", description: "").save();
		Organization PIH = new Organization(name: "Partners In Health", description: "").save();
	
		/* Condition Type */
		ConditionType AIDS_HIV = new ConditionType(name: "HIV/AIDS").save();
		ConditionType CANCER = new ConditionType(name: "Cancer").save();
		ConditionType DIARRHEA = new ConditionType(name: "Diarrhea").save();
		ConditionType PAIN = new ConditionType(name: "Pain").save();
		ConditionType TUBERCULOSIS = new ConditionType(name: "Tuberculosis").save();
		
		/* Product Type */
		ProductType MEDICINES = new ProductType(parent: null, name: "Medicines").save();
		ProductType SUPPLIES = new ProductType(parent: null, name: "Supplies").save();
		ProductType EQUIPMENT = new ProductType(parent: null, name: "Equipment").save();
		ProductType PERISHABLES = new ProductType(parent: null, name: "Perishables").save();
		ProductType OTHER = new ProductType(parent: null, name: "Other").save();
		
		/* Product Type > Supplies */
		ProductType MEDICAL_SUPPLIES = new ProductType(parent: SUPPLIES, name: "Medical Supplies").save();
		ProductType HOSPITAL_SUPPLIES = new ProductType(parent: SUPPLIES, name: "Hospital and Clinic Supplies").save();
		ProductType OFFICE_SUPPLIES = new ProductType(parent: SUPPLIES, name: "Office Supplies").save();

		/* Product Type > Equipment */
		ProductType MEDICAL_EQUIPMENT = new ProductType(parent: EQUIPMENT, name: "Medical Equipment").save();
		ProductType SURGICAL_EQUIPMENT = new ProductType(parent: EQUIPMENT, name: "Surgical Equipment").save();
		ProductType TECH_EQUIPMENT = new ProductType(parent: EQUIPMENT, name: "IT Equipment").save();
		ProductType FURNITURE = new ProductType(parent: EQUIPMENT, name: "Furniture and Equipment").save();

		/* Product Type > Food */
		ProductType FOOD = new ProductType(parent: PERISHABLES, name: "Food").save();
		
		/* Product Type > Medicines */
		ProductType MEDICINES_ARV = new ProductType(parent: MEDICINES, name: "ARVS").save();
		ProductType MEDICINES_ANESTHESIA = new ProductType(parent: MEDICINES, name: "Anesteshia").save();
		ProductType MEDICINES_CANCER = new ProductType(parent: MEDICINES, name: "Cancer").save();
		ProductType MEDICINES_CHRONIC_CARE = new ProductType(parent: MEDICINES, name: "Chronic Care").save();
		ProductType MEDICINES_PAIN = new ProductType(parent: MEDICINES, name: "Pain").save();
		ProductType MEDICINES_TB = new ProductType(parent: MEDICINES, name: "TB").save();
		ProductType MEDICINES_OTHER = new ProductType(parent: MEDICINES, name: "Other").save();
		
		/* Product Type > Medical Supplies */
		ProductType MED_SUPPLIES_LAB = new ProductType(parent: MEDICAL_SUPPLIES, name: "Lab").save();
		ProductType MED_SUPPLIES_SURGICAL = new ProductType(parent: MEDICAL_SUPPLIES, name: "Surgical").save();
		ProductType MED_SUPPLIES_XRAY = new ProductType(parent: MEDICAL_SUPPLIES, name: "X-Ray").save();
		ProductType MED_SUPPLIES_DENTAL = new ProductType(parent: MEDICAL_SUPPLIES, name: "Dental").save();
		ProductType MED_SUPPLIES_OTHER = new ProductType(parent: MEDICAL_SUPPLIES, name: "Other").save();
		
     	/* Shipment Container Type */
     	ContainerType CONTAINER = new ContainerType(name:"Container").save();
     	ContainerType PALLET = new ContainerType(name:"Pallet").save();
     	ContainerType LARGE_BOX = new ContainerType(name:"Large Box").save();
     	ContainerType MEDIUM_BOX = new ContainerType(name:"Medium Box").save();
     	ContainerType SMALL_BOX = new ContainerType(name:"Small Box").save();
     	ContainerType TRUNK = new ContainerType(name:"Trunk").save();
     	ContainerType SUITCASE = new ContainerType(name:"Suitcase").save();
     	ContainerType ITEM = new ContainerType(name:"Item").save();
     	     		 		 	
		/* Shipment Status */	 	
     	ShipmentStatus NOT_SHIPPED = new ShipmentStatus(name:"Not shipped", description: "Has not shipped yet", finalStatus:false).save();
	 	ShipmentStatus IN_TRANSIT = new ShipmentStatus(name:"In transit", description: "In transit to destination", finalStatus:false).save();
	 	ShipmentStatus IN_CUSTOMS = new ShipmentStatus(name:"In customs", description: "Being inspected by customer", finalStatus:false).save();
	 	ShipmentStatus DELIVERED = new ShipmentStatus(name:"Delivered", description: "Delivered to destination", finalStatus:true).save();	
	 	ShipmentStatus CONFIRMED = new ShipmentStatus(name:"Confirmed", description: "Delivered to destination", finalStatus:true).save();	
	 	
	 	/* Inventory Status */
		//InventoryStatus IN_STOCK = new InventoryStatus()
	 	//InventoryStatus LOW_STOCK = new InventoryStatus();
		//InventoryStatus OUT_OF_STOCK = new InventoryStatus()
		//InventoryStatus ON_BACKORDER = new InventoryStatus()
	 	//InventoryStatus STOCK_AVAILABLE = new InventoryStatus();
	 	//InventoryStatus UNAVAILABLE = new InventoryStatus();
	 	
     	/* Shipment Event Type */     	
     	EventType ORDER_RECEIVED = new EventType(name:"ORDER_RECEIVED", description:"Order has been received").save();
     	EventType ORDER_PICKED = new EventType(name:"ORDER_PICKED", description:"Order is being packed").save();
     	EventType SHIPMENT_PACKED = new EventType(name:"SHIPMENT_PACKED", description:"Shipment is packed").save();     	
     	EventType SHIPMENT_LOADED = new EventType(name:"SHIPMENT_LOADED", description:"Shipment has been loaded into truck").save();
     	EventType SHIPMENT_SENT = new EventType(name:"SHIPMENT_SENT", description:"Shipment has been sent by shipper").save();
     	EventType SHIPMENT_IN_TRANSIT = new EventType(name:"SHIPMENT_IN_TRANSIT", description:"Shipment has departed").save();
     	EventType SHIPMENT_DELIVERED = new EventType(name:"SHIPMENT_DELIVERED", description:"Shipment has been delivered by the carrier").save();
     	EventType SHIPMENT_RECEIVED = new EventType(name:"SHIPMENT_RECEIVED", description:"Shipment has been received by the recipient").save();
     	EventType SHIPMENT_UNLOADED = new EventType(name:"SHIPMENT_UNLOADED", description:"Shipment has arrived").save();
     	EventType SHIPMENT_STAGED = new EventType(name:"SHIPMENT_STAGED", description:"Shipment has arrived").save();
     	EventType SHIPMENT_UNPACKED = new EventType(name:"SHIPMENT_UNPACKED", description:"Shipment has arrived").save();
     	EventType SHIPMENT_STORED = new EventType(name:"SHIPMENT_STORED", description:"Shipment has been stored in warehouse").save();
     	
     	/* Reference Number Type */
     	// Unique internal identifier, PO Number, Bill of Lading Number, or customer name,      	
     	ReferenceType PO_NUMBER = new ReferenceType(name: "PO_NUMBER", description: "Purchase Order Number").save();
     	ReferenceType CUSTOMER_NAME = new ReferenceType(name: "CUSTOMER_NAME", description: "Customer name").save();
     	ReferenceType INTERNAL_IDENTIFIER = new ReferenceType(name: "INTERNAL_IDENTIFIER", description: "Internal Identifier").save();
     	ReferenceType BILL_OF_LADING_NUMBER = new ReferenceType(name: "BILL_OF_LADING", description: "Bill of Lading Number").save();
     	     	
     			
		/* Shipment methods */	
	 	ShipmentMethod FEDEX_AIR = new ShipmentMethod(	 		
 			name:"FedEx Air",
 			methodName:"fedex", 			
 			trackingNumberFormat:"999999999999", 
 			trackingUrl:"http://www.fedex.com/Tracking?ascend_header=1&clienttype=dotcom&cntry_code=us&language=english&tracknumbers=%s",
 			trackingUrlParameterName:""
	 	).save();
		
	 	ShipmentMethod FEDEX_GROUND = new ShipmentMethod(	 		
 			name:"FedEx Ground",
 			methodName:"fedex", 			
 			trackingNumberFormat:"999999999999", 
 			trackingUrl:"http://www.fedex.com/Tracking?ascend_header=1&clienttype=dotcom&cntry_code=us&language=english&tracknumbers=%s",
 			trackingUrlParameterName:""
	 	).save();
	 	
	 	ShipmentMethod UPS_GROUND = new ShipmentMethod(
	 		name:"UPS Ground", 
 			methodName:"ups",
 			trackingNumberFormat:"1Z9999W99999999999", 
	 		trackingUrl:"http://wwwapps.ups.com/WebTracking/processInputRequest?sort_by=status&tracknums_displayed=1&TypeOfInquiryNumber=T&loc=en_US&InquiryNumber1=%s&track.x=0&track.y=0",
 			trackingUrlParameterName:""
	 	).save();
	 	
	 	ShipmentMethod USPS_GROUND = new ShipmentMethod(
	 		name:"US Postal Service Ground Service", 
 			methodName:"usps",
 			trackingNumberFormat:"", 
	 		trackingUrl:"", 
	 		trackingUrlParameterName:""
	 	).save();
	 	
	 	ShipmentMethod COURIER = new ShipmentMethod(
	 		name:"Courier",
	 		methodName:"courier"
	 		//trackingUrl:"http://www.google.com/search?hl=en&site=&q=",
	 	).save();
	 	
	 	/* Transaction types */
		TransactionType INCOMING = new TransactionType(
				name:"Incoming Shipment").save(flush:true, validate:true);
		
		TransactionType OUTGOING = new TransactionType(
				name:"Outgoing Shipment").save(flush:true, validate:true);
		
		TransactionType DONATION = new TransactionType(
				name:"Donation").save(flush:true, validate:true);
          	

	 	
	 	
	 	
	 	/*
	 	ProductLookup product = new ProductLookup(
	 		methodName:"UPC Lookup",
	 		trackingUrl:"http://www.upcdatabase.com/item/${product.ean}"	 		
	 	)*/
	 	
		
		/* Users */		
		User supervisor = new User(
			email:"supervisor@pih.org", 
			firstName:"Miss", 
			lastName:"Supervisor",
			role:"Supervisor", 
			username:"super", 
			password: "password" 
		).save();
		User manager = new User(
			email:"manager@pih.org", 
			firstName:"Mister", 
			lastName:"Manager",
			role:"Manager", 
			username:"manager", 
			password: "password",
			manager: supervisor
		).save();
		User jmiranda = new User(
			email:"jmiranda@pih.org", 
			firstName:"Justin", 
			lastName:"Miranda",
			role:"Stocker", 
			username:"jmiranda", 
			password: "password",
			manager: manager
		).save();


		
		/* Products */
		Product advil = new DrugProduct(ean:"ADVIL", productCode:"1", name:"Advil 200mg", type: MEDICINES, subType: MEDICINES_PAIN).save(flush:true);
		Product tylenol = new DrugProduct(ean:"TYLENOL",productCode:"2", name: "Tylenol 325mg", type: MEDICINES, subType: MEDICINES_PAIN).save(flush:true);		
		Product aspirin = new DrugProduct(ean:"ASPIRIN",productCode:"3", name: "Aspirin 20mg", type: MEDICINES, subType: MEDICINES_PAIN).save(flush:true);
		Product generic = new DrugProduct(ean:"GENERIC PAIN", productCode:"4", name: "Generic Pain Reliever",type: MEDICINES, subType: MEDICINES_PAIN).save(flush:true)
		Product didanosine = new DrugProduct(ean: "DIDANOSINE", productCode:"5", name:"Didanosine 200mg", type: MEDICINES, subType: MEDICINES_ARV).save(flush:true);
		
		advil.addToConditionTypes(PAIN).save(flush:true);
		tylenol.addToConditionTypes(PAIN).save(flush:true);
		aspirin.addToConditionTypes(PAIN).save(flush:true);
		generic.addToConditionTypes(PAIN).save(flush:true);
		didanosine.addToConditionTypes(AIDS_HIV).save(flush:true);
		didanosine.addToCategories(ROOT_CATEGORY).save(flush:true);
		
		
		/* Addresses */
		Address address1 = new Address(address: "888 Commonwealth Avenue",address2: "Third Floor",city:"Boston",stateOrProvince:"Massachusetts",postalCode: "02215",country: "United States").save(flush:true)
		Address address2 = new Address(address: "1000 State Street",address2: "Building A",city: "Miami",stateOrProvince: "Florida",postalCode: "33126",country: "United States").save(flush:true);
		Address address3 = new Address(address: "12345 Main Street", address2: "Suite 401", city: "Tabarre", stateOrProvince: "", postalCode: "", country: "Haiti").save(flush:true);
		
		/* Warehouses */
		Warehouse boston = new Warehouse(name: "Boston Headquarters", address: address1, manager: manager).save(flush:true);		
		Warehouse miami = new Warehouse(name: "Miami Warehouse", address: address2, manager: manager).save(flush:true);
		Warehouse tabarre = new Warehouse(name: "Tabarre Depot", address: address3, manager: manager).save(flush:true);
		
		/** 
		 * Warehouse > Inventory > Inventory items 
		 */
		
		// Create new inventory
		Inventory tabarreInventory = new Inventory(warehouse:tabarre, lastInventoryDate: new Date()).save(flush:true);
		
		// Create new inventory item
		InventoryItem inventoryItem1 = new InventoryItem(product: advil, quantity: 100, reorderQuantity: 50, idealQuantity: 100, binLocation: "Warehouse Bin A1").save(flush:true);
		InventoryItem inventoryItem2 = new InventoryItem(product: tylenol, quantity: 200, reorderQuantity: 50, idealQuantity: 100, binLocation: "Warehouse Bin A1").save(flush:true);

		// Add to inventory
		tabarreInventory.addToInventoryItems(inventoryItem1).save(flush:true, validate:false);
		tabarreInventory.addToInventoryItems(inventoryItem2).save(flush:true, validate:false);
		
		/** 
		 * Warehouse > Transactions > Transaction Entries 
		 */
		
		Transaction transaction1 = new Transaction(transactionDate:new Date(), targetWarehouse:tabarre, transactionType:INCOMING); // removed .save(flush:true);
		tabarre.addToTransactions(transaction1).save();
		
		TransactionEntry transactionEntry1 = new TransactionEntry(product: advil, quantityChange:50, confirmDate:new Date());
		transaction1.addToTransactionEntries(transactionEntry1).save(flush:true, validate:false);
		
		
		/* Create a new shipment */		
		Shipment shipment1 = new Shipment(
			reference: "000000232",
			referenceType:	INTERNAL_IDENTIFIER,
			comment: "ship me as soon as possible",
			shipmentStatus: NOT_SHIPPED,
			shipmentMethod: UPS_GROUND,
			trackingNumber: "1Z9999W99999999999",
			expectedShippingDate : Date.parse("yyyy-MM-dd", "2010-06-01"),
			expectedDeliveryDate : Date.parse("yyyy-MM-dd", "2010-06-06"),
			origin : boston,
			destination : miami
		).save(flush:true);	
		
		ShipmentEvent event1 = new ShipmentEvent(eventType:ORDER_RECEIVED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-25 14:00:00"), eventLocation: boston)
		ShipmentEvent event2 = new ShipmentEvent(eventType:ORDER_PICKED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-25 15:30:00"), eventLocation: boston)
		ShipmentEvent event3 = new ShipmentEvent(eventType:SHIPMENT_PACKED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-25 17:45:00"), eventLocation: boston)
		ShipmentEvent event4 = new ShipmentEvent(eventType:SHIPMENT_LOADED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-26 09:00:00"), eventLocation: boston)
		ShipmentEvent event5 = new ShipmentEvent(eventType:SHIPMENT_SENT, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-26 11:00:00"), eventLocation: boston, targetLocation: miami)

		shipment1.addToEvents(event1).save(flush:true);
		shipment1.addToEvents(event2).save(flush:true);
		shipment1.addToEvents(event3).save(flush:true);
		shipment1.addToEvents(event4).save(flush:true);
		shipment1.addToEvents(event5).save(flush:true);
		
		shipment1.addToReferenceNumbers(new ReferenceNumber(identifier:"0002492910", referenceType: PO_NUMBER)).save(flush:true)
		shipment1.addToReferenceNumbers(new ReferenceNumber(identifier:"00001", referenceType: INTERNAL_IDENTIFIER)).save(flush:true)
		
		Document document1 = new Document(filename: "shipment-packing-list.pdf", type: "Packing List", size: 1020L, contents: "empty")
		shipment1.addToDocuments(document1).save(flush:true);		
		
		Document document2 = new Document(filename: "shipment-invoice.pdf", type: "Invoice", size: 990L, contents: "empty") 
		shipment1.addToDocuments(document2).save(flush:true);
		
		Container pallet1 = new Container(
			name: "My container",
			containerType: PALLET,
			weight: 1000,
			units: "kgs"
		);
		shipment1.addToContainers(pallet1).save(flush:true);

		
		ShipmentItem shipmentItem1 = new ShipmentItem(product : advil, quantity : 100, packageType: LARGE_BOX);
		pallet1.addToShipmentItems(shipmentItem1).save(flush:true);
		
		ShipmentItem shipmentItem2 = new ShipmentItem(product : tylenol, quantity : 200, packageType: LARGE_BOX);
		pallet1.addToShipmentItems(shipmentItem2).save(flush:true);

		ShipmentItem shipmentItem3 = new ShipmentItem(product : aspirin, quantity : 300, packageType: LARGE_BOX);
		pallet1.addToShipmentItems(shipmentItem3).save(flush:true);
		
		
		def destroy = {
		
		}
		
	}
	
}
