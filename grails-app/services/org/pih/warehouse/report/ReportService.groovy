package org.pih.warehouse.report;

import groovy.xml.SAXBuilder;

import java.util.Map;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.grails.plugins.excelimport.ExcelImportUtils;
import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.ProductAttribute;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.core.Constants 
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.Errors;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.FSEntityResolver;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.extend.XhtmlCssOnlyNamespaceHandler;
import org.xml.sax.EntityResolver;

import org.pih.warehouse.reporting.Consumption;

class ReportService implements ApplicationContextAware {
	
	def sessionFactory
	def productService
	def inventoryService
	def shipmentService
	def localizationService
	
	ApplicationContext applicationContext
	
	boolean transactional = true
	
	
	public void generateShippingReport(ChecklistReportCommand command) {		
		def shipmentItems = command?.shipment?.shipmentItems?.sort()
		shipmentItems.each { shipmentItem -> 
			command.checklistReportEntryList << new ChecklistReportEntryCommand(shipmentItem: shipmentItem)
		}
	
	}
	
	
	public void generateProductReport(ProductReportCommand command) { 
		
		command.inventoryItems = InventoryItem.findAllByProduct(command?.product)
		command.quantityInitial = inventoryService.getInitialQuantity(command?.product, command?.location, command?.startDate)

		def transactionEntries = inventoryService.getTransactionEntries(command?.product, command?.location, command?.startDate, command?.endDate)
				
		// Calculate quantity at each transaction entry point.
		def quantity = command?.quantityInitial;
		transactionEntries.each { transactionEntry ->
			def productReportEntry = new ProductReportEntryCommand(transactionEntry: transactionEntry, balance: 0)
			productReportEntry.balance = inventoryService.adjustQuantity(quantity, transactionEntry)
			command.productReportEntryList << productReportEntry
			
			// Need to keep track of the running total so we can adjust the balance as we go
			quantity = productReportEntry.balance
		}
		//command.quantityFinal = quantity;
		command.quantityFinal = inventoryService.getCurrentQuantity(command?.product, command?.location, command?.endDate)

	}
	
	
	TransactionEntry getEarliestTransactionEntry(Product product, Inventory inventory) { 
		def list = TransactionEntry.createCriteria().list() {
			and { 
				inventoryItem {
					eq("product.id", product?.id)
				}
				transaction {
					eq("inventory", inventory)
					order("transactionDate", "asc")
					order("dateCreated", "asc")
				}
			}
			maxResults(1)
		}
		
		return list[0]
	}
	
	TransactionEntry getLatestTransactionEntry(Product product, Inventory inventory) { 
		def list = TransactionEntry.createCriteria().list() {
			and {
				inventoryItem {
					eq("product.id", product?.id)
				}
				transaction {
					eq("inventory", inventory)
					order("transactionDate", "desc")
					order("dateCreated", "desc")
				}
			}
			maxResults(1)
		}
		return list[0]
	}
	
	/**
	 * 
	 * @param command
	 */
	public void generateTransactionReport(InventoryReportCommand command) { 
		
		def products = 
			//inventoryService.getProductsByNestedCategory(command.category)
			(command?.includeChildren) ? inventoryService.getProductsByNestedCategory(command.category) : 
				inventoryService.getProductsByCategory(command.category) 
				
		if (command?.showEntireHistory) { 
			def earliestDate = getEarliestTransactionEntry(command?.product, command?.location?.inventory)?.transaction?.transactionDate			
			//def latestDate = getLatestTransactionEntry(command?.product, command?.location?.inventory)?.transaction?.transactionDate
			command.startDate = earliestDate?:command.startDate
			command.endDate = new Date() + 1
		}
		
		// 
		//command.startDate = command.startDate?:new Date()
		//command.endDate = command.endDate?:new Date()
		// TODO Need to restrict by date and category
		def transactionEntries = inventoryService.getTransactionEntries(command.location, command.category, command?.startDate, command?.endDate);
		def transactionEntriesByProduct = transactionEntries.groupBy { it?.inventoryItem?.product }

				
		log.info "Products (" + products.size() + ") -> " + products
		// Initialize the report map to reference all products to be displayed		 
		products.each { product ->
			
			def productTransactionEntries = transactionEntriesByProduct[product]
			def includeProduct = (command?.hideInactiveProducts && productTransactionEntries || !command?.hideInactiveProducts)
				
			if (includeProduct) { 
				def productEntry = command.entries[product];
				if (!productEntry) {
					productEntry = new InventoryReportEntryCommand(product: product);
					command.entries[product] = productEntry
				}
				productEntry.quantityInitial = inventoryService.getInitialQuantity(product, command?.location, command?.startDate?:null)
				productEntry.quantityFinal = inventoryService.getCurrentQuantity(product, command?.location, command?.endDate?:new Date());
	
				// Initialize the product map to reference all inventory items for that product
				def inventoryItems = inventoryService.getInventoryItemsByProduct(product)
				inventoryItems?.each { inventoryItem ->				
					//log.info "inventory item -> " + inventoryItem
					def inventoryItemEntry = productEntry?.entries[inventoryItem];
					if (!inventoryItemEntry) { 
						inventoryItemEntry = new InventoryReportEntryCommand(product: product, inventoryItem: inventoryItem);
						productEntry.entries[inventoryItem] = inventoryItemEntry;
					}
					inventoryItemEntry.quantityInitial = inventoryService.getQuantity(inventoryItem, command.location, command.startDate?:null)
					inventoryItemEntry.quantityFinal = inventoryService.getQuantity(inventoryItem, command.location, command.endDate?:new Date())
					inventoryItemEntry.quantityRunning = inventoryItemEntry.quantityInitial
					
					//inventoryItemEntry.quantityInitial = inventoryService.getInitialQuantity(inventoryItem, command?.location, command?.startDate)
					//inventoryItemEntry.quantityFinal = inventoryService.getCurrentQuantity(inventoryItem, command?.location, command?.endDate);
					
				}
			}
		}
		
		
		log.info "transactionEntries (" + transactionEntries.size() + ") -> " + transactionEntries
		// Iterate over the transaction entries for the given time period to tabulate totals.
		// Each time we encounter an INVENTORY, compare that quantity with the running total,
		// and add / subract to "adjustment" as appropriate.  Then set the running
		// total to the new inventory and continue with the running total...
		transactionEntries.each {
			def inventoryItem = it?.inventoryItem
			def transactionType = it?.transaction?.transactionType
			
			log.info "transactionEntry -> " + it.transaction.transactionType.name + " = " + it.quantity
			
			def productEntry = command.entries[inventoryItem.product]
			if (productEntry) { 
				def inventoryItemEntry = productEntry.entries[inventoryItem];		
						
				
				if (inventoryItemEntry) {					
					
					if (transactionType?.id == Constants.CONSUMPTION_TRANSACTION_TYPE_ID) {
						inventoryItemEntry.quantityRunning += it.quantity
						inventoryItemEntry.quantityConsumed += it.quantity
						inventoryItemEntry.quantityTotalOut += it.quantity
					}
					else if (transactionType?.id == Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID) {
						inventoryItemEntry.quantityRunning += it.quantity
						inventoryItemEntry.quantityFound += it.quantity
						inventoryItemEntry.quantityAdjusted += it.quantity
						inventoryItemEntry.quantityTotalIn += it.quantity
					}
					else if (transactionType?.id == Constants.EXPIRATION_TRANSACTION_TYPE_ID) {
						inventoryItemEntry.quantityRunning -= it.quantity
						inventoryItemEntry.quantityExpired += it.quantity
						inventoryItemEntry.quantityTotalOut += it.quantity
					}
					else if (transactionType?.id == Constants.DAMAGE_TRANSACTION_TYPE_ID) {
						inventoryItemEntry.quantityRunning -= it.quantity
						inventoryItemEntry.quantityDamaged += it.quantity
						inventoryItemEntry.quantityTotalOut += it.quantity
					}
					else if (transactionType?.id == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID) {
						inventoryItemEntry.quantityRunning += it.quantity
						inventoryItemEntry.quantityTransferredIn += it.quantity
						inventoryItemEntry.quantityTotalIn += it.quantity
						if (!inventoryItemEntry.quantityTransferredInByLocation[it.transaction.source]) { 
							inventoryItemEntry.quantityTransferredInByLocation[it.transaction.source] = 0
						}
						inventoryItemEntry.quantityTransferredInByLocation[it.transaction.source] += it.quantity					
					}
					else if (transactionType?.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID) {
						inventoryItemEntry.quantityRunning -= it.quantity
						inventoryItemEntry.quantityTransferredOut += it.quantity
						inventoryItemEntry.quantityTotalOut += it.quantity
						if (!inventoryItemEntry.quantityTransferredOutByLocation[it.transaction.destination]) { 
							inventoryItemEntry.quantityTransferredOutByLocation[it.transaction.destination] = 0;
						}
						inventoryItemEntry.quantityTransferredOutByLocation[it.transaction.destination] += it.quantity					
					}
					else if (transactionType?.id == Constants.ADJUSTMENT_DEBIT_TRANSACTION_TYPE_ID) {
						inventoryItemEntry.quantityRunning -= it.quantity
						inventoryItemEntry.quantityLost += it.quantity
						inventoryItemEntry.quantityAdjusted -= it.quantity
						inventoryItemEntry.quantityTotalOut += it.quantity
					}
					else if (transactionType?.id == Constants.INVENTORY_TRANSACTION_TYPE_ID) {
						def diff = it.quantity - inventoryItemEntry.quantityRunning
						inventoryItemEntry.quantityAdjusted += diff					
						inventoryItemEntry.quantityRunning = it.quantity;
						if (diff > 0) { 
							inventoryItemEntry.quantityFound += diff;
							inventoryItemEntry.quantityTotalIn += diff
						}
						else {  
							inventoryItemEntry.quantityLost += diff	
							inventoryItemEntry.quantityTotalOut += diff
						}
					}
					else if (transactionType?.id == Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID) {
						def diff = it.quantity - inventoryItemEntry.quantityRunning
						inventoryItemEntry.quantityAdjusted += diff
						inventoryItemEntry.quantityRunning = it.quantity;
						if (diff > 0) { 
							inventoryItemEntry.quantityFound += diff;
							inventoryItemEntry.quantityTotalIn += diff
						}
						else {  
							inventoryItemEntry.quantityLost += diff	
							inventoryItemEntry.quantityTotalOut += diff
						}
					}
					
					// Add transaction entry
					def balance = inventoryItemEntry.quantityRunning
					inventoryItemEntry.transactionEntries << new ProductReportEntryCommand(transactionEntry: it, balance: balance)
					
				}
			}
		}		
	}	
	
	/**
	 * Generate a PDF of the page at the given URL and write to the given output stream.
	 * 
	 * @param url
	 * @param outputStream
	 * @return
	private generatePdf(String url, OutputStream outputStream) {		
		log.info "Generate pdf from page at URL " + url
		ITextRenderer renderer = new ITextRenderer();
		Document document = getDocument(url);
		renderer.setDocument(document, "");
		renderer.layout();
		renderer.createPDF(outputStream);
	}
	 */
	
	void generatePdf(String url, OutputStream outputStream) { 
		log.info "Generate PDF for URL " + url
		try { 
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocument(url);
			renderer.layout();
			renderer.createPDF(outputStream);
		} catch (Exception e) { 
			log.error("Cannot generate pdf due to error " + e.message)
		}
	}
	
	/*
	void generatePdf(String url, OutputStream outputStream) { 
		SAXBuilder sb = new SAXBuilder();
		// This is needed to avoid a Connection timeout on the DTD
		EntityResolver er = sb.getEntityResolver();
		if (er == null) {
			sb.setEntityResolver(FSEntityResolver.instance());
		}
		DOMOutputter outputter = new DOMOutputter();
		org.jdom.Document html = sb.build(new File(inputFileName));
		org.w3c.dom.Document htmlDoc = outputter.output(html);

		//OutputStream os = new FileOutputStream(outputFileName);
		//String url = new File(inputFileName).toURI().toURL().toString();
		
		ITextRenderer renderer = new ITextRenderer();
		renderer.setDocument(htmlDoc, url, new XhtmlCssOnlyNamespaceHandler());
		renderer.layout();
		renderer.createPDF(outputStream);
		outputStream.close();		
	}
	*/
	
	/*	
	def downloadFile(url) {
		def file = new FileOutputStream(url.tokenize("/")[-1])
		def out = new BufferedOutputStream(file)
		out << new URL(url).openStream()
		out.close()
	}*/
	

	
	
	private getDocument(String url) { 
		Document document = null;
		InputStream inputStream = null;
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setValidating(false);
			
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();			
			
			// Get source input stream
			String xhtml = getHtmlContent(url)
			
			inputStream = new ByteArrayInputStream(xhtml.getBytes());
			
			// Use FS's local cached XML entities so we don't have to hit the web.
			builder.setEntityResolver(FSEntityResolver.instance());
			
			// Parse input stream into document
			document = builder.parse(inputStream);
			
			// Alternative approach 
			//document = XMLResource.load(new ByteArrayInputStream(xhtml.getBytes())).getDocument();
			
			
		} finally {
			if (inputStream) inputStream.close();
		}

		return document;
		
	}
	
	
	private getHtmlContent(String url) { 
		
		HttpClient httpclient = new DefaultHttpClient();
		try {
			HttpGet httpget = new HttpGet(url);
			// Create a response handler
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);
			return responseBody;
			
			
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
	}

	
}
