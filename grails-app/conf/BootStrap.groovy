import java.util.ArrayList;
import java.util.Date;
import org.pih.warehouse.Address;
import org.pih.warehouse.Attribute;
import org.pih.warehouse.Document;
import org.pih.warehouse.Category;
import org.pih.warehouse.Country;
import org.pih.warehouse.Comment;
import org.pih.warehouse.Container;
import org.pih.warehouse.ConditionType;
import org.pih.warehouse.ContainerType;
import org.pih.warehouse.ConsumableProduct;
import org.pih.warehouse.DocumentType;
import org.pih.warehouse.DrugProduct;
import org.pih.warehouse.DrugRouteType;
import org.pih.warehouse.DurableProduct;
import org.pih.warehouse.EventType;
import org.pih.warehouse.GenericType;
import org.pih.warehouse.Inventory;
import org.pih.warehouse.InventoryItem;
import org.pih.warehouse.Organization;
import org.pih.warehouse.Product;
import org.pih.warehouse.ProductAttributeValue;
import org.pih.warehouse.ProductType;
import org.pih.warehouse.ReferenceNumberType;
import org.pih.warehouse.ReferenceNumber;
import org.pih.warehouse.StockCard;
import org.pih.warehouse.Shipment;
import org.pih.warehouse.ShipmentEvent;
import org.pih.warehouse.ShipmentItem;
import org.pih.warehouse.ShipmentMethod;
import org.pih.warehouse.ShipmentStatus;
import org.pih.warehouse.SupplierType;
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
		
		/* Countries */	 	
		Country CANADA = new Country(country: "Canada", population: 24251210, gdp: 24251210, date: new Date()).save();
		Country HAITI = new Country(country: "Haiti", population: 29824821, gdp: 24251210, date: new Date()).save();
		Country MEXICO = new Country(country: "Mexico", population: 103593973, gdp: 24251210, date: new Date()).save();
		Country USA = new Country(country: "United States", population: 300000000, gdp: 24251210, date: new Date()).save();
		
		/* Organizations */
		Organization ZL = new Organization(name:  "Zanmi Lasante", description: "").save();
		Organization PIH = new Organization(name: "Partners In Health", description: "").save();
			
		/* Category > Top Level */
		Category CATEGORY_MEDICINES = new Category(parent: null, name: "Medicines").save();
		Category CATEGORY_SUPPLIES = new Category(parent: null, name: "Supplies").save();
		Category CATEGORY_EQUIPMENT = new Category(parent: null, name: "Equipment").save();
		Category CATEGORY_PERISHABLES = new Category(parent: null, name: "Perishables").save();
		Category CATEGORY_OTHER = new Category(parent: null, name: "Other").save();
		
		/* Category > Supplies */
		Category CATEGORY_MEDICAL_SUPPLIES = new Category(parent: CATEGORY_SUPPLIES, name: "Medical Supplies").save();
		Category CATEGORY_HOSPITAL_SUPPLIES = new Category(parent: CATEGORY_SUPPLIES, name: "Hospital and Clinic Supplies").save();
		Category CATEGORY_OFFICE_SUPPLIES = new Category(parent: CATEGORY_SUPPLIES, name: "Office Supplies").save();
		
		/* Category > Equipment */
		Category CATEGORY_MEDICAL_EQUIPMENT = new Category(parent: CATEGORY_EQUIPMENT, name: "Medical Equipment").save();
		Category CATEGORY_SURGICAL_EQUIPMENT = new Category(parent: CATEGORY_EQUIPMENT, name: "Surgical Equipment").save();
		Category CATEGORY_TECH_EQUIPMENT = new Category(parent: CATEGORY_EQUIPMENT, name: "IT Equipment").save();
		Category CATEGORY_FURNITURE = new Category(parent: CATEGORY_EQUIPMENT, name: "Furniture and Equipment").save();
		
		/* Category > Food */
		Category CATEGORY_FOOD = new Category(parent: CATEGORY_PERISHABLES, name: "Food").save();
		
		/* Category > Medicines */ 
		Category CATEGORY_MEDICINES_ARV = new Category(parent: CATEGORY_MEDICINES, name: "ARVS").save();
		Category CATEGORY_MEDICINES_ANESTHESIA = new Category(parent: CATEGORY_MEDICINES, name: "Anesteshia").save();
		Category CATEGORY_MEDICINES_CANCER = new Category(parent: CATEGORY_MEDICINES, name: "Cancer").save();
		Category CATEGORY_MEDICINES_CHRONIC_CARE = new Category(parent: CATEGORY_MEDICINES, name: "Chronic Care").save();
		Category CATEGORY_MEDICINES_PAIN = new Category(parent: CATEGORY_MEDICINES, name: "Pain").save();
		Category CATEGORY_MEDICINES_TB = new Category(parent: CATEGORY_MEDICINES, name: "TB").save();
		Category CATEGORY_MEDICINES_OTHER = new Category(parent: CATEGORY_MEDICINES, name: "Other").save();
		
		/* Category > Medical Supplies */ 
		Category CATEGORY_MED_SUPPLIES_LAB = new Category(parent: CATEGORY_MEDICAL_SUPPLIES, name: "Lab").save();
		Category CATEGORY_MED_SUPPLIES_SURGICAL = new Category(parent: CATEGORY_MEDICAL_SUPPLIES, name: "Surgical").save();
		Category CATEGORY_MED_SUPPLIES_XRAY = new Category(parent: CATEGORY_MEDICAL_SUPPLIES, name: "X-Ray").save();
		Category CATEGORY_MED_SUPPLIES_DENTAL = new Category(parent: CATEGORY_MEDICAL_SUPPLIES, name: "Dental").save();
		Category CATEGORY_MED_SUPPLIES_OTHER = new Category(parent: CATEGORY_MEDICAL_SUPPLIES, name: "Other").save();		
		
		/* Condition Type */
		ConditionType CONDITION_AIDS_HIV = new ConditionType(name: "HIV/AIDS").save();
		ConditionType CONDITION_CANCER = new ConditionType(name: "Cancer").save();
		ConditionType CONDITION_DIARRHEA = new ConditionType(name: "Diarrhea").save();
		ConditionType CONDITION_PAIN = new ConditionType(name: "Pain").save();
		ConditionType CONDITION_TUBERCULOSIS = new ConditionType(name: "Tuberculosis").save();
		
		
		/**
		 * Airway Bill
		 * Bill of Lading
		 * Packing List
		 * Certificate of Donation
		 * Commercial Invoice
		 * Material Safety Data Sheet
		 * Certificate of Analysis
		 * Manifest
		 * Other
		 */
		DocumentType DOCUMENT_AIRWAY_BILL = new DocumentType(name: "Airway Bill").save();
		DocumentType DOCUMENT_BILL_OF_LADING = new DocumentType(name: "Bill of Lading").save();
		DocumentType DOCUMENT_PACKING_LIST = new DocumentType(name: "Packing List").save();
		DocumentType DOCUMENT_CERTIFICATE_OF_DONATION = new DocumentType(name: "Certificate of Donation").save();
		DocumentType DOCUMENT_COMMERCIAL_INVOICE = new DocumentType(name: "Commercial Invoice").save();
		DocumentType DOCUMENT_MATERIAL_SAFETY_DATA_SHEET = new DocumentType(name: "Material Safety Data Sheet").save();
		DocumentType DOCUMENT_CERTICATE_OF_ANALYSIS = new DocumentType(name: "Certificate of Analysis").save();
		DocumentType DOCUMENT_MANIFEST = new DocumentType(name: "Manifest").save();
		DocumentType DOCUMENT_OTHER = new DocumentType(name: "Other").save();
		
		
		
		/* Generic Type */
		GenericType GENERIC_LAPTOP = new GenericType(name: "Laptop").save();
		GenericType GENERIC_GLOVE = new GenericType(name: "Glove").save();
		GenericType GENERIC_GUAZE = new GenericType(name: "Guaze").save();
		GenericType GENERIC_TISSUE = new GenericType(name: "Tissue").save();
		GenericType GENERIC_FOOTWEAR = new GenericType(name: "Shoe").save();
		GenericType GENERIC_ARV_MEDICATION = new GenericType(name: "ARV Medication").save();
		GenericType GENERIC_PAIN_MEDICATION = new GenericType(name: "Pain Medication").save();
		GenericType GENERIC_VEGETABLE = new GenericType(name: "Vegetable").save();
		GenericType GENERIC_ELECTRONICS = new GenericType(name: "Electronics").save();
		
		/* Attribute Vitality */
		Attribute ATTRIBUTE_VITALITY = new Attribute(name:"Vitality", dataType: "String").save(flush:true);
		Value VALUE_VITAL = new Value(stringValue:"vital").save();
		Value VALUE_ESSENTIAL = new Value(stringValue:"essential").save();
		Value VALUE_NON_ESSENTIAL = new Value(stringValue:"non-essential").save();
		ATTRIBUTE_VITALITY.addToValues(VALUE_VITAL).save(flush:true);
		ATTRIBUTE_VITALITY.addToValues(VALUE_ESSENTIAL).save(flush:true);
		ATTRIBUTE_VITALITY.addToValues(VALUE_NON_ESSENTIAL).save(flush:true);

		/* Attribute Size */
		Attribute ATTRIBUTE_SIZE = new Attribute(name: "Size", dataType: "String").save();
		Value VALUE_SMALL = new Value(stringValue:"small").save();
		Value VALUE_MEDIUM = new Value(stringValue:"medium").save();
		Value VALUE_LARGE = new Value(stringValue:"large").save();
		Value VALUE_EXTRA_LARGE = new Value(stringValue:"x-large").save();			
		ATTRIBUTE_SIZE.addToValues(VALUE_SMALL).save(flush:true);
		ATTRIBUTE_SIZE.addToValues(VALUE_MEDIUM).save(flush:true);
		ATTRIBUTE_SIZE.addToValues(VALUE_LARGE).save(flush:true);
		ATTRIBUTE_SIZE.addToValues(VALUE_EXTRA_LARGE).save(flush:true);
		
		/* Product Types */
		ProductType PRODUCT_MEDS_ARV = new ProductType(parent: null, name: "Meds-ARV").save();
		ProductType PRODUCT_MEDS_TB = new ProductType(parent: null, name: "Meds-TB").save();
		ProductType PRODUCT_MEDS_CANCER = new ProductType(parent: null, name: "Meds-Cancer").save();
		ProductType PRODUCT_MEDS_CHRONIC_CARE = new ProductType(parent: null, name: "Meds-Chronic Care").save();
		ProductType PRODUCT_MEDS_ANESTHESIA = new ProductType(parent: null, name: "Meds-Anesthesia").save();
		ProductType PRODUCT_MEDS_NARCOTICS = new ProductType(parent: null, name: "Meds-Narcotics").save();
		ProductType PRODUCT_MEDS_OTHER = new ProductType(parent: null, name: "Meds-Other").save();
		ProductType PRODUCT_MED_SUPPLIES_LAB = new ProductType(parent: null, name: "Med Supplies-Lab").save();
		ProductType PRODUCT_MED_SUPPLIES_SURGICAL = new ProductType(parent: null, name: "Med Supplies-Surgical").save();
		ProductType PRODUCT_MED_SUPPLIES_XRAY = new ProductType(parent: null, name: "Med Supplies-X-ray").save();
		ProductType PRODUCT_MED_SUPPLIES_DENTAL = new ProductType(parent: null, name: "Med Supplies-Dental").save();
		ProductType PRODUCT_MED_SUPPLIES_OTHER = new ProductType(parent: null, name: "Med Supplies-Other").save();
		ProductType PRODUCT_MEDICAL_EQUIPMENT = new ProductType(parent: null, name: "Medical Equipment").save();
		ProductType PRODUCT_SURGICAL_EQUIPMENT = new ProductType(parent: null, name: "Surgical Equipment").save();
		ProductType PRODUCT_HOSPITAL_SUPPLIES = new ProductType(parent: null, name: "Hospital and Clinic Supplies").save();
		ProductType PRODUCT_FURNITURE_AND_EQUIPMENT = new ProductType(parent: null, name: "Furniture and Equipment").save();
		ProductType PRODUCT_IT_AND_OFFICE = new ProductType(parent: null, name: "IT and Office").save();
		ProductType PRODUCT_FOOD = new ProductType(parent: null, name: "Food").save();
		ProductType PRODUCT_OTHER = new ProductType(parent: null, name: "Other").save();
		
		/* Product Type: Root, Supplies, Equipment, Food
		ProductType MEDICINES = new ProductType(parent: null, name: "Medicines").save();
		ProductType MEDICINES_ARV = new ProductType(parent: MEDICINES, name: "ARVS").save();
		ProductType MEDICINES_ANESTHESIA = new ProductType(parent: MEDICINES, name: "Anesteshia").save();
		ProductType MEDICINES_CANCER = new ProductType(parent: MEDICINES, name: "Cancer").save();
		ProductType MEDICINES_CHRONIC_CARE = new ProductType(parent: MEDICINES, name: "Chronic Care").save();
		ProductType MEDICINES_PAIN = new ProductType(parent: MEDICINES, name: "Pain").save();
		ProductType MEDICINES_TB = new ProductType(parent: MEDICINES, name: "TB").save();
		ProductType MEDICINES_OTHER = new ProductType(parent: MEDICINES, name: "Other").save();
		ProductType SUPPLIES = new ProductType(parent: null, name: "Supplies").save();
		ProductType MEDICAL_SUPPLIES = new ProductType(parent: SUPPLIES, name: "Medical Supplies").save();
		ProductType MED_SUPPLIES_LAB = new ProductType(parent: MEDICAL_SUPPLIES, name: "Lab").save();
		ProductType MED_SUPPLIES_SURGICAL = new ProductType(parent: MEDICAL_SUPPLIES, name: "Surgical").save();
		ProductType MED_SUPPLIES_XRAY = new ProductType(parent: MEDICAL_SUPPLIES, name: "X-Ray").save();
		ProductType MED_SUPPLIES_DENTAL = new ProductType(parent: MEDICAL_SUPPLIES, name: "Dental").save();
		ProductType MED_SUPPLIES_OTHER = new ProductType(parent: MEDICAL_SUPPLIES, name: "Other").save();
		ProductType HOSPITAL_SUPPLIES = new ProductType(parent: SUPPLIES, name: "Hospital and Clinic Supplies").save();
		ProductType OFFICE_SUPPLIES = new ProductType(parent: SUPPLIES, name: "Office Supplies").save();
		ProductType EQUIPMENT = new ProductType(parent: null, name: "Equipment").save();
		ProductType MEDICAL_EQUIPMENT = new ProductType(parent: EQUIPMENT, name: "Medical Equipment").save();
		ProductType SURGICAL_EQUIPMENT = new ProductType(parent: EQUIPMENT, name: "Surgical Equipment").save();
		ProductType TECH_EQUIPMENT = new ProductType(parent: EQUIPMENT, name: "IT Equipment").save();
		ProductType FURNITURE = new ProductType(parent: EQUIPMENT, name: "Furniture and Equipment").save();
		ProductType PERISHABLES = new ProductType(parent: null, name: "Perishables").save();
		ProductType FOOD = new ProductType(parent: PERISHABLES, name: "Food").save();
		ProductType OTHER = new ProductType(parent: null, name: "Other").save();
		*/
		
		/* Routes of Administration 
		 * PO      Oral (per os, by mouth)
		 * PR      Rectal (per rectum, by the rectum)
		 * IM      Intramuscular
		 * IV      Intravenous
		 * SC      Subcutaneous
		 * SL      Sublingual
		 */		
		DrugRouteType DRUG_ROUTE_ORAL = new DrugRouteType(name: "Oral").save();
		DrugRouteType DRUG_ROUTE_RECTAL = new DrugRouteType(name: "Rectal").save();
		DrugRouteType DRUG_ROUTE_INTRAMUSCULAR = new DrugRouteType(name: "Intramuscular").save();
		DrugRouteType DRUG_ROUTE_INTRAVENOUS = new DrugRouteType(name: "Intravenous").save();
		DrugRouteType DRUG_ROUTE_SUBCUTANEOUS = new DrugRouteType(name: "Subcutaneous").save();
		DrugRouteType DRUG_ROUTE_SUBLINGUAL = new DrugRouteType(name: "Sublingual").save();
			
		/* Shipment Container Type */
		ContainerType PALLET = new ContainerType(name:"Pallet").save();
		ContainerType SUITCASE = new ContainerType(name:"Suitcase").save();
		ContainerType LARGE_BOX = new ContainerType(name:"Large Box").save();
		ContainerType MEDIUM_BOX = new ContainerType(name:"Medium Box").save();
		ContainerType SMALL_BOX = new ContainerType(name:"Small Box").save();
		ContainerType TRUNK = new ContainerType(name:"Trunk").save();
		ContainerType ITEM = new ContainerType(name:"Single Item").save();
		ContainerType CONTAINER = new ContainerType(name:"Container").save();
		ContainerType CONTAINER_OTHER = new ContainerType(name:"Other").save();
				
		/* Shipment Status */	 	
		ShipmentStatus SHIPMENT_NOT_SHIPPED = new ShipmentStatus(name:"Not shipped", description: "Has not shipped yet", finalStatus:false).save();
		ShipmentStatus SHIPMENT_IN_TRANSIT = new ShipmentStatus(name:"In transit", description: "In transit to destination", finalStatus:false).save();
		ShipmentStatus SHIPMENT_IN_CUSTOMS = new ShipmentStatus(name:"In customs", description: "Being inspected by customer", finalStatus:false).save();
		ShipmentStatus SHIPMENT_DELIVERED = new ShipmentStatus(name:"Delivered", description: "Delivered to destination", finalStatus:true).save();	
		ShipmentStatus SHIPMENT_CONFIRMED = new ShipmentStatus(name:"Confirmed", description: "Delivered to destination", finalStatus:true).save();	
		
		/* Inventory Status */
		//InventoryStatus IN_STOCK = new InventoryStatus()
		//InventoryStatus LOW_STOCK = new InventoryStatus();
		//InventoryStatus OUT_OF_STOCK = new InventoryStatus()
		//InventoryStatus ON_BACKORDER = new InventoryStatus()
		//InventoryStatus STOCK_AVAILABLE = new InventoryStatus();
		//InventoryStatus UNAVAILABLE = new InventoryStatus();
		
		/** 
		 * Shipment Event Type 
		 */     	
		EventType EVENT_ORDER_RECEIVED = new EventType(name:"Order has been received", description:"Order has been received").save();
		EventType EVENT_ORDER_PICKED = new EventType(name:"Order is being picked", description:"Order is being picked").save();
		EventType EVENT_SHIPMENT_PACKED = new EventType(name:"Shipment is being packed", description:"Shipment is packed").save();     	
		EventType EVENT_SHIPMENT_LOADED = new EventType(name:"Shipment is being packed onto truck", description:"Shipment has been loaded into truck").save();
		EventType EVENT_SHIPMENT_SENT = new EventType(name:"Shipment has been sent via shipper", description:"Shipment has been sent by shipper").save();
		EventType EVENT_SHIPMENT_IN_TRANSIT = new EventType(name:"Shipment is in transit", description:"Shipment has departed").save();
		EventType EVENT_SHIPMENT_DELIVERED = new EventType(name:"Shipment has been delivered by shipper", description:"Shipment has been delivered by the carrier").save();
		EventType EVENT_SHIPMENT_RECEIVED = new EventType(name:"Shipment has been received", description:"Shipment has been received by the recipient").save();
		EventType EVENT_SHIPMENT_UNLOADED = new EventType(name:"Shipment has been unloaded", description:"Shipment has arrived").save();
		EventType EVENT_SHIPMENT_STAGED = new EventType(name:"Shipment has been staged", description:"Shipment has arrived").save();
		EventType EVENT_SHIPMENT_UNPACKED = new EventType(name:"Shipment has been unpacked", description:"Shipment has arrived").save();
		EventType EVENT_SHIPMENT_STORED = new EventType(name:"Shipment has been stored", description:"Shipment has been stored in warehouse").save();
		
		/** 
		 * Reference Number Type 
		 */
		// Unique internal identifier, PO Number, Bill of Lading Number, or customer name,      	
		ReferenceNumberType REFERENCE_PO_NUMBER = new ReferenceNumberType(name: "Purchase Order Number", description: "Purchase Order Number").save();
		ReferenceNumberType REFERENCE_CUSTOMER_NAME = new ReferenceNumberType(name: "Customer Name", description: "Customer name").save();
		ReferenceNumberType REFERENCE_INTERNAL_IDENTIFIER = new ReferenceNumberType(name: "Internal Identifier", description: "Internal Identifier").save();
		ReferenceNumberType REFERENCE_BILL_OF_LADING_NUMBER = new ReferenceNumberType(name: "Bill of Lading Number", description: "Bill of Lading Number").save();
		

		SupplierType SUPPLIER_LOCAL = new SupplierType(name: "LOCAL", description: "Local supplier").save();
		SupplierType SUPPLIER_INTERNATIONAL = new SupplierType(name: "INTERNATIONAL", description: "International supplier").save();
		SupplierType SUPPLIER_NATIONAL = new SupplierType(name: "NATIONAL", description: "National supplier").save();
		SupplierType SUPPLIER_OEM = new SupplierType(name: "OEM", description: "Original equipment manufacturer").save();
		SupplierType SUPPLIER_OTHER = new SupplierType(name: "OTHER", description: "Other").save();
		
		/** 
		 * Shipment methods 
		 */	
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
			name:"USPS Ground", 
			methodName:"usps",
			trackingNumberFormat:"", 
			trackingUrl:"", 
			trackingUrlParameterName:"").save();
		
		ShipmentMethod COURIER = new ShipmentMethod(name:"Courier", methodName:"courier"
			//trackingUrl:"http://www.google.com/search?hl=en&site=&q=",
			).save();
		
		/** 
		 * Transaction types 
		 */
		TransactionType TRANSACTION_INCOMING = new TransactionType(name:"Incoming").save(flush:true, validate:true);		
		TransactionType TRANSACTION_OUTGOING = new TransactionType(name:"Outgoing").save(flush:true, validate:true);		
		TransactionType TRANSACTION_DONATION = new TransactionType(name:"Donation").save(flush:true, validate:true);
		
		/** 
		 * Users 
		 */		
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
		
		/**
		 * Products > Attributes
		 */
		ProductAttributeValue productVitality = new ProductAttributeValue(attribute: ATTRIBUTE_VITALITY, allowMultiple: Boolean.FALSE)
		productVitality.addToValues(VALUE_ESSENTIAL);		
		
		/**
		 * Products > Pain Medications
		 */
		Product advil = new DrugProduct(ean:"AD00001VIL", productCode:"00001", name:"Advil 200mg", genericName: "Ibuprofen", productType: PRODUCT_MEDS_OTHER).save(flush:true);
		advil.addToConditionTypes(CONDITION_PAIN).save(flush:true);		
		advil.addToProductAttributeValues(productVitality).save(flush:true);
		advil.addToCategories(CATEGORY_MEDICINES).save(flush:true);
		advil.addToCategories(CATEGORY_MEDICINES_PAIN).save(flush:true);

		Product tylenol = new DrugProduct(ean:"TY00006LENOL",productCode:"00006", name: "Tylenol 325mg", genericName: "Ibuprofen", productType: PRODUCT_MEDS_OTHER).save(flush:true);		
		tylenol.addToConditionTypes(CONDITION_PAIN).save(flush:true);
		tylenol.addToCategories(CATEGORY_MEDICINES).save(flush:true);
		tylenol.addToCategories(CATEGORY_MEDICINES_PAIN).save(flush:true);
		
		Product aspirin = new DrugProduct(ean:"AS00007PIRIN",productCode:"00007", name: "Aspirin 20mg", genericName: "Ibuprofen", productType: PRODUCT_MEDS_OTHER).save(flush:true);
		aspirin.addToConditionTypes(CONDITION_PAIN).save(flush:true);
		aspirin.addToCategories(CATEGORY_MEDICINES);
		aspirin.addToCategories(CATEGORY_MEDICINES_PAIN);
		
		Product generic = new DrugProduct(ean:"GENERAL00008PAIN", productCode:"00008", name: "General Pain Reliever", genericName: "Ibuprofen", productType: PRODUCT_MEDS_OTHER).save(flush:true)
		generic.addToConditionTypes(CONDITION_PAIN).save(flush:true);
		generic.addToCategories(CATEGORY_MEDICINES);
		generic.addToCategories(CATEGORY_MEDICINES_PAIN);
		
		/*
		Product genpril = new DrugProduct(ean:"GEN00002PRIL", productCode:"00002", name:"Genpril", genericName: "Ibuprofen", productType: PRODUCT_MEDS_OTHER).save(flush:true);
		genpril.addToConditionTypes(CONDITION_PAIN).save(flush:true);
		genpril.addToCategories(CATEGORY_MEDICINES).save(flush:true);
		genpril.addToCategories(CATEGORY_MEDICINES_PAIN).save(flush:true);
		
		Product midol = new DrugProduct(ean:"MI00003DOL", productCode:"00003", name:"Midol", genericName: "Ibuprofen", productType: PRODUCT_MEDS_OTHER).save(flush:true);
		midol.addToConditionTypes(CONDITION_PAIN).save(flush:true);
		midol.addToCategories(CATEGORY_MEDICINES).save(flush:true);
		midol.addToCategories(CATEGORY_MEDICINES_PAIN).save(flush:true);
		
		Product motrin = new DrugProduct(ean:"MOT00004RIN", productCode:"00004", name:"Motrin", genericName: "Ibuprofen", productType: PRODUCT_MEDS_OTHER).save(flush:true);
		motrin.addToConditionTypes(CONDITION_PAIN).save(flush:true);
		motrin.addToCategories(CATEGORY_MEDICINES).save(flush:true);
		motrin.addToCategories(CATEGORY_MEDICINES_PAIN).save(flush:true);
		
		Product nuprin = new DrugProduct(ean:"NUP00005RIN", productCode:"00005", name:"Nuprin", genericName: "Ibuprofen", productType: PRODUCT_MEDS_OTHER).save(flush:true);
		nuprin.addToConditionTypes(CONDITION_PAIN).save(flush:true);
		nuprin.addToCategories(CATEGORY_MEDICINES).save(flush:true);
		nuprin.addToCategories(CATEGORY_MEDICINES_PAIN).save(flush:true);
		
		*/
		
		
		/**
		 * Products > HIV Medications
		Product didanosine = new DrugProduct(ean: "DIDAN00009OSINE", productCode:"00009", name:"Didanosine 200mg", productType: PRODUCT_MEDS_OTHER).save(flush:true);		
		didanosine.addToConditionTypes(CONDITION_AIDS_HIV).save(flush:true);
		didanosine.addToCategories(CATEGORY_MEDICINES).save(flush:true);
		didanosine.addToCategories(CATEGORY_MEDICINES_ARV).save(flush:true);
		*/
		
		/**
		 * Products > Durable goods 
		Product reflotron = new DurableProduct(ean: "RE00010FLOTRON", productCode: "00010", name: "Reflotron", productType: CATEGORY_SURGICAL_EQUIPMENT, make: "Roche", model: "", serialNumber: "4004388").save(flush:true); 
		reflotron.addToCategories(CATEGORY_EQUIPMENT).save(flush:true);
		reflotron.addToCategories(CATEGORY_SURGICAL_EQUIPMENT).save(flush:true);
		 */
		
		/**
		 * Products to add ...
		 *  
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
		 * -- Meds-Narcotic ---------------------------------
		 * Fentanyl 0.1 mg/ 2 ml, for injection
		 * Morphine sulphate 30 mg, controlled release 
		 * Morphine sulfate 10 mg/ml, 1 ml, for injection
		 * Phenobarbital 50mg 
		 * Phenobarbital sodium 200 mg/ 2 ml, for injection
		 */
		
		/** 
		 * Food products 
		 */
		Product similacAdvanceLowIron = new ConsumableProduct(name: "Similac Advance low iron 400g").save(flush:true);
		similacAdvanceLowIron.addToCategories(CATEGORY_FOOD).save(flush:true);
				
		Product similacAdvancePlusIron = new ConsumableProduct(name: "Similac Advance + iron 365g").save(flush:true);		
		similacAdvancePlusIron.addToCategories(CATEGORY_FOOD).save(flush:true);
		
		/** 
		 * Addresses 
		 */
		Address address1 = new Address(address: "888 Commonwealth Avenue",address2: "Third Floor",city:"Boston",stateOrProvince:"Massachusetts",postalCode: "02215",country: "United States").save(flush:true)
		Address address2 = new Address(address: "1000 State Street",address2: "Building A",city: "Miami",stateOrProvince: "Florida",postalCode: "33126",country: "United States").save(flush:true);
		Address address3 = new Address(address: "12345 Main Street", address2: "Suite 401", city: "Tabarre", stateOrProvince: "", postalCode: "", country: "Haiti").save(flush:true);
		
		/** 
		 * Warehouses 
		 */
		Warehouse boston = new Warehouse(name: "Boston Headquarters", address: address1, manager: manager).save(flush:true);		
		Warehouse miami = new Warehouse(name: "Miami Warehouse", address: address2, manager: manager).save(flush:true);
		Warehouse tabarre = new Warehouse(name: "Tabarre Depot", address: address3, manager: manager).save(flush:true);
		
		/** 
		 * Warehouse > Inventory > Inventory items 
		 */		
		InventoryItem inventoryItem1 = new InventoryItem(product: advil, quantity: 100, reorderQuantity: 50, idealQuantity: 100, binLocation: "Warehouse Bin A1").save(flush:true);
		InventoryItem inventoryItem2 = new InventoryItem(product: tylenol, quantity: 200, reorderQuantity: 50, idealQuantity: 100, binLocation: "Warehouse Bin A1").save(flush:true);		

		Inventory tabarreInventory = new Inventory(warehouse:tabarre, lastInventoryDate: new Date()).save(flush:true);		
		tabarreInventory.addToInventoryItems(inventoryItem1).save(flush:true, validate:false);
		tabarreInventory.addToInventoryItems(inventoryItem2).save(flush:true, validate:false);
		
		/** 
		 * Warehouse > Transactions > Transaction Entries 
		 */
		
		Transaction transaction1 = new Transaction(transactionDate:new Date(), targetWarehouse:tabarre, transactionType:TRANSACTION_INCOMING); // removed .save(flush:true);
		tabarre.addToTransactions(transaction1).save();
		
		TransactionEntry transactionEntry1 = new TransactionEntry(product: advil, quantityChange:50, confirmDate:new Date());
		transaction1.addToTransactionEntries(transactionEntry1).save(flush:true, validate:false);
		
		/* Create a new shipment */		
		Shipment shipment1 = new Shipment(name: "New Shipment 1", description: "Contains boxes of stuff",				
			shipmentStatus: SHIPMENT_NOT_SHIPPED, shipmentMethod: UPS_GROUND, trackingNumber: "1Z9999W99999999999",
			expectedShippingDate : Date.parse("yyyy-MM-dd", "2010-06-01"), origin : boston,
			expectedDeliveryDate : Date.parse("yyyy-MM-dd", "2010-06-05"), destination : miami).save(flush:true);	

		Shipment shipment2 = new Shipment(name: "New Shipment 2", description: "Contains boxes of stuff",				
			shipmentStatus: SHIPMENT_NOT_SHIPPED, shipmentMethod: UPS_GROUND, trackingNumber: "1Z9999W99999999999",
			expectedShippingDate : Date.parse("yyyy-MM-dd", "2010-06-05"), origin : miami,
			expectedDeliveryDate : null, destination : tabarre).save(flush:true);	
		
		Shipment shipment3 = new Shipment(name: "New Shipment 3", description: "Contains boxes of stuff",				
			shipmentStatus: SHIPMENT_NOT_SHIPPED, shipmentMethod: UPS_GROUND, trackingNumber: "1Z9999W99999999999", 
			expectedShippingDate : Date.parse("yyyy-MM-dd", "2010-06-05"), origin : miami, 
			expectedDeliveryDate : null, destination : boston).save(flush:true);	
		
		Comment comment1 = new Comment(comment: "We need to ship this as soon as possible!", commenter: jmiranda, recipient: jmiranda, sendDate: new Date()).save(flush:true);
		shipment1.addToComments(comment1).save(flush:true);

		Comment comment2 = new Comment(comment: "Did you ship this yet?!?!?!?", commenter: manager, recipient: jmiranda, sendDate: new Date()).save(flush:true);
		shipment2.addToComments(comment1).save(flush:true);

		Comment comment3 = new Comment(comment: "What is taking so long?", commenter: supervisor, recipient: jmiranda, sendDate: new Date()).save(flush:true);
		shipment3.addToComments(comment1).save(flush:true);

		
		ShipmentEvent event1 = new ShipmentEvent(eventType:EVENT_ORDER_RECEIVED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-25 14:00:00"), eventLocation: boston)
		ShipmentEvent event2 = new ShipmentEvent(eventType:EVENT_ORDER_PICKED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-25 15:30:00"), eventLocation: boston)
		ShipmentEvent event3 = new ShipmentEvent(eventType:EVENT_SHIPMENT_PACKED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-25 17:45:00"), eventLocation: boston)
		ShipmentEvent event4 = new ShipmentEvent(eventType:EVENT_SHIPMENT_LOADED, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-26 09:00:00"), eventLocation: boston)
		ShipmentEvent event5 = new ShipmentEvent(eventType:EVENT_SHIPMENT_SENT, eventDate: Date.parse("yyyy-MM-dd hh:mm:ss", "2010-05-26 11:00:00"), eventLocation: boston, targetLocation: miami)
		
		shipment1.addToEvents(event1).save(flush:true);
		shipment1.addToEvents(event2).save(flush:true);
		shipment1.addToEvents(event3).save(flush:true);
		shipment1.addToEvents(event4).save(flush:true);
		shipment1.addToEvents(event5).save(flush:true);
		
		shipment1.addToReferenceNumbers(new ReferenceNumber(identifier:"0002492910", referenceNumberType:REFERENCE_PO_NUMBER)).save(flush:true)
		shipment1.addToReferenceNumbers(new ReferenceNumber(identifier:"0000000001", referenceNumberType:REFERENCE_INTERNAL_IDENTIFIER)).save(flush:true)
		
		Document document1 = new Document(filename: "shipment-packing-list.pdf", documentType: DOCUMENT_PACKING_LIST, size: 1020L, contents: "empty")
		shipment1.addToDocuments(document1).save(flush:true);		
		
		Document document2 = new Document(filename: "shipment-invoice.pdf", documentType: DOCUMENT_COMMERCIAL_INVOICE, size: 990L, contents: "empty") 
		shipment1.addToDocuments(document2).save(flush:true);
		
		Container pallet1 = new Container(name: "Pallet #1", containerType: PALLET, weight: 1000, units: "kgs");
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
