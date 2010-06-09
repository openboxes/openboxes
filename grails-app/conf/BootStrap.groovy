import java.util.ArrayList;
import java.util.Date;
import org.pih.warehouse.Address;
import org.pih.warehouse.Attribute;
import org.pih.warehouse.Document;
import org.pih.warehouse.Category;
import org.pih.warehouse.Country;
import org.pih.warehouse.Container;
import org.pih.warehouse.ConditionType;
import org.pih.warehouse.ContainerType;
import org.pih.warehouse.ConsumableProduct;
import org.pih.warehouse.EventType;
import org.pih.warehouse.GenericType;
import org.pih.warehouse.Inventory;
import org.pih.warehouse.InventoryItem;
import org.pih.warehouse.Organization;
import org.pih.warehouse.DrugProduct;
import org.pih.warehouse.DurableProduct;
import org.pih.warehouse.Product;
import org.pih.warehouse.ProductAttributeValue;
import org.pih.warehouse.ProductType;
import org.pih.warehouse.ReferenceNumberType;
import org.pih.warehouse.ReferenceNumber;
import org.pih.warehouse.DrugRouteType;
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
import org.pih.warehouse.Value;
import org.pih.warehouse.Warehouse;

class BootStrap {
	
	/**
	 * 
	 */
	def init = { servletContext ->
		
		/* Categories */
		Category ROOTCATEGORY = new Category(parent: null, name: "Root").save();
		Category SUBCATEGORYA = new Category(parent: ROOTCATEGORY, name: "Sub A").save();
		Category SUBCATEGORYB = new Category(parent: ROOTCATEGORY, name: "Sub B").save();
		Category SUBCATEGORYC = new Category(parent: ROOTCATEGORY, name: "Sub C").save();
		Category SUBCATEGORYA1 = new Category(parent: SUBCATEGORYA, name: "Sub A1").save();
		Category SUBCATEGORYA2 = new Category(parent: SUBCATEGORYA, name: "Sub A2").save();
		Category SUBCATEGORYA3 = new Category(parent: SUBCATEGORYA, name: "Sub A3").save();
		Category SUBCATEGORYC1 = new Category(parent: SUBCATEGORYC, name: "Sub C1").save();
		Category SUBCATEGORYC3 = new Category(parent: SUBCATEGORYC, name: "Sub C3").save();
		
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
		
		/* Generic Type */
		GenericType LAPTOP = new GenericType(name: "Laptop").save();
		GenericType GLOVE = new GenericType(name: "Glove").save();
		GenericType GUAZE = new GenericType(name: "Guaze").save();
		GenericType TISSUE = new GenericType(name: "Tissue").save();
		GenericType FOOTWEAR = new GenericType(name: "Shoe").save();
		GenericType ARV_MEDICATION = new GenericType(name: "ARV Medication").save();
		GenericType PAIN_MEDICATION = new GenericType(name: "Pain Medication").save();
		GenericType VEGETABLE = new GenericType(name: "Vegetable").save();
		GenericType ELECTRONICS = new GenericType(name: "Electronics").save();
		
		/* Attributes */
		Attribute vitality = new Attribute(name:"Vitality", dataType: "String").save(flush:true);
		Value vital = new Value(stringValue:"vital")
		Value essential = new Value(stringValue:"essential")
		Value nonEssential = new Value(stringValue:"non-essential")
		vitality.addToValues(vital).save(flush:true);
		vitality.addToValues(essential).save(flush:true);
		vitality.addToValues(nonEssential).save(flush:true);
						
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
		
		/* Routes of Administration */
		/*
		 * PO      Oral (per os, by mouth)
		 * PR      Rectal (per rectum, by the rectum)
		 * IM      Intramuscular
		 * IV      Intravenous
		 * SC      Subcutaneous
		 * SL      Sublingual
		 */		
		DrugRouteType ORAL = new DrugRouteType(name: "Oral").save();
		DrugRouteType RECTAL = new DrugRouteType(name: "Rectal").save();
		DrugRouteType INTRAMUSCULAR = new DrugRouteType(name: "Intramuscular").save();
		DrugRouteType INTRAVENOUS = new DrugRouteType(name: "Intravenous").save();
		DrugRouteType SUBCUTANEOUS = new DrugRouteType(name: "Subcutaneous").save();
		DrugRouteType SUBLINGUAL = new DrugRouteType(name: "Sublingual").save();
			
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
		ReferenceNumberType PO_NUMBER = new ReferenceNumberType(name: "PO_NUMBER", description: "Purchase Order Number").save();
		ReferenceNumberType CUSTOMER_NAME = new ReferenceNumberType(name: "CUSTOMER_NAME", description: "Customer name").save();
		ReferenceNumberType INTERNAL_IDENTIFIER = new ReferenceNumberType(name: "INTERNAL_IDENTIFIER", description: "Internal Identifier").save();
		ReferenceNumberType BILL_OF_LADING_NUMBER = new ReferenceNumberType(name: "BILL_OF_LADING", description: "Bill of Lading Number").save();
		
		
		/* Shipment methods */	
		ShipmentMethod FEDEX_AIR = new ShipmentMethod(	 		
			name:"FedEx Air",
			methodName:"fedex", 			
			trackingNumberFormat:"999999999999", 
			trackingUrl:"http://www.fedex.com/Tracking?ascend_header=1&clienttype=dotcom&cntry_code=us&language=english&tracknumbers=%s",
			trackingUrlParameterName:"").save();
		
		ShipmentMethod FEDEX_GROUND = new ShipmentMethod(	 		
			name:"FedEx Ground",
			methodName:"fedex", 			
			trackingNumberFormat:"999999999999", 
			trackingUrl:"http://www.fedex.com/Tracking?ascend_header=1&clienttype=dotcom&cntry_code=us&language=english&tracknumbers=%s",
			trackingUrlParameterName:"").save();
		
		ShipmentMethod UPS_GROUND = new ShipmentMethod(
			name:"UPS Ground", 
			methodName:"ups",
			trackingNumberFormat:"1Z9999W99999999999", 
			trackingUrl:"http://wwwapps.ups.com/WebTracking/processInputRequest?sort_by=status&tracknums_displayed=1&TypeOfInquiryNumber=T&loc=en_US&InquiryNumber1=%s&track.x=0&track.y=0",
			trackingUrlParameterName:"").save();
		
		ShipmentMethod USPS_GROUND = new ShipmentMethod(
			name:"US Postal Service Ground Service", 
			methodName:"usps",
			trackingNumberFormat:"", 
			trackingUrl:"", 
			trackingUrlParameterName:"").save();
		
		ShipmentMethod COURIER = new ShipmentMethod(name:"Courier", methodName:"courier"
			//trackingUrl:"http://www.google.com/search?hl=en&site=&q=",
			).save();
		
		/* Transaction types */
		TransactionType INCOMING = new TransactionType(name:"Incoming Shipment").save(flush:true, validate:true);		
		TransactionType OUTGOING = new TransactionType(name:"Outgoing Shipment").save(flush:true, validate:true);		
		TransactionType DONATION = new TransactionType(name:"Donation").save(flush:true, validate:true);
		
		/* Users */		
		User supervisor = new User(
			email:"supervisor@pih.org", 
			firstName:"Miss", 
			lastName:"Supervisor",
			role:"Supervisor", 
			username:"super", 
			password: "password").save();
		User manager = new User(
			email:"manager@pih.org", 
			firstName:"Mister", 
			lastName:"Manager",
			role:"Manager", 
			username:"manager", 
			password: "password",
			manager: supervisor).save();
		User jmiranda = new User(
			email:"jmiranda@pih.org", 
			firstName:"Justin", 
			lastName:"Miranda",
			role:"Stocker", 
			username:"jmiranda", 
			password: "password",
			manager: manager).save();
		
		
		
		/* Products */
		
		/**
		 * Pain Medications
		 */
		Product advil = new DrugProduct(ean:"ADVIL-00001", productCode:"00001", name:"Advil 200mg", genericName: "Ibuprofen", type: MEDICINES, subType: MEDICINES_PAIN).save(flush:true);
		assert advil != null;
		advil.addToConditionTypes(PAIN).save(flush:true);
		
		ProductAttributeValue advilVitality = new ProductAttributeValue(attribute: vitality, allowMultiple: Boolean.FALSE)
		advilVitality.addToValues(essential);
		advil.addToProductAttributeValues(advilVitality).save(flush:true);
		
		Product genpril = new DrugProduct(ean:"GEN00002PRIL", productCode:"00002", name:"Genpril", genericName: "Ibuprofen", type: MEDICINES, subType: MEDICINES_PAIN).save(flush:true);
		genpril.addToConditionTypes(PAIN).save(flush:true);

		Product midol = new DrugProduct(ean:"MI00003DOL", productCode:"00003", name:"Midol", genericName: "Ibuprofen", type: MEDICINES, subType: MEDICINES_PAIN).save(flush:true);
		midol.addToConditionTypes(PAIN).save(flush:true);
		
		Product motrin = new DrugProduct(ean:"MOT00004RIN", productCode:"00004", name:"Motrin", genericName: "Ibuprofen", type: MEDICINES, subType: MEDICINES_PAIN).save(flush:true);
		motrin.addToConditionTypes(PAIN).save(flush:true);
		
		Product nuprin = new DrugProduct(ean:"NUP00005RIN", productCode:"00005", name:"Nuprin", genericName: "Ibuprofen", type: MEDICINES, subType: MEDICINES_PAIN).save(flush:true);
		nuprin.addToConditionTypes(PAIN).save(flush:true);
		
		Product tylenol = new DrugProduct(ean:"TY00006LENOL",productCode:"00006", name: "Tylenol 325mg", genericName: "Ibuprofen", type: MEDICINES, subType: MEDICINES_PAIN).save(flush:true);		
		tylenol.addToConditionTypes(PAIN).save(flush:true);
		
		Product aspirin = new DrugProduct(ean:"AS00007PIRIN",productCode:"00007", name: "Aspirin 20mg", genericName: "Ibuprofen", type: MEDICINES, subType: MEDICINES_PAIN).save(flush:true);
		aspirin.addToConditionTypes(PAIN).save(flush:true);
		
		Product generic = new DrugProduct(ean:"GENERAL00008PAIN", productCode:"00008", name: "General Pain Reliever", genericName: "Ibuprofen", type: MEDICINES, subType: MEDICINES_PAIN).save(flush:true)
		generic.addToConditionTypes(PAIN).save(flush:true);
		
		
		
		/**
		 * HIV Medications
		 */
		Product didanosine = new DrugProduct(ean: "DIDAN00009OSINE", productCode:"00009", name:"Didanosine 200mg", type: MEDICINES, subType: MEDICINES_ARV).save(flush:true);		
		assert didanosine

		didanosine.addToConditionTypes(AIDS_HIV).save(flush:true);
		didanosine.addToCategories(ROOTCATEGORY).save(flush:true);
		
		
		
		/* 
		 * -- HIV-Meds
		 * Didanosine 200mg
		 * Lamivudine 150mg
		 * Stavudine 40mg
		 * Zidovudine 10mg/ml solution
		 * Nevirapine 10mg/ml solution
		 * Lamivudine 10mg/ml solution
		 * 
		 * -- TB-Meds
		 * Isoniazide 100mg
		 * Isoniazide 300mg
		 * Pyrazinamide 400mg
		 * Rifampicin 150mg
		 * Rifampicin 300mg
		 * Rifampicin 150mg + isoniazide 75mg
		 * Rifampicin 300mg + Isoniazide 150mg
		 * 
		 * 
		 * 
		 * -- Meds-Narcotic ---------------------------------
		 * Fentanyl 0.1 mg/ 2 ml, for injection
		 * Morphine sulphate 30 mg, controlled release 
		 * Morphine sulfate 10 mg/ml, 1 ml, for injection
		 * Phenobarbital 50mg 
		 * Phenobarbital sodium 200 mg/ 2 ml, for injection
		 */
		
		/* Food products */
		Product similacAdvanceLowIron = new ConsumableProduct(name: "Similac Advance low iron 400g");
		Product similacAdvancePlusIron = new ConsumableProduct(name: "Similac Advance + iron 365g");		
		
		
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
			destination : miami).save(flush:true);	
	
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
