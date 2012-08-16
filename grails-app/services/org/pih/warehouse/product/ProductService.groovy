package org.pih.warehouse.product

import java.io.ExpiringCache.Entry;
import java.util.List;

import groovy.xml.Namespace;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.importer.ImportDataCommand;

import com.amazon.advertising.api.sample.SignedRequestsHelper;


/**
 * Keys
 * 
 * HIPAASpace.com: 6BB8325D3C4F42AEBDC8F9584CA85C8D79815FA7F6194AD79793BF512981E84B
 * Google: AIzaSyCAEGyY6QpPbm3DiHmtx6qIZ_P40FnF3vk
 * Amazon:
 * RXNorm(http://rxnav.nlm.nih.gov/REST/): bff71b0439e75797f6af27b220eefe7b9b0b989d
 * 
 * @author jmiranda
 *
 */
class ProductService {
	
	def grailsApplication
	
	private static final String ENDPOINT = "ecs.amazonaws.com";
	private static final String AWS_SECRET_KEY = "put your secret key here";
	private static final String AWS_ACCESS_KEY_ID = "put your access key here";
	
	def findAmazonProducts() { 
		SignedRequestsHelper helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
		
	    String requestUrl = null;
	    String title = null;
	
	    Map params = new HashMap();
	    params.put("Service", "AWSECommerceService");
	    params.put("Version", "2009-03-31");
	    params.put("Operation", "ItemLookup");
	    params.put("ItemId", asin);
	    params.put("ResponseGroup", "Small,Medium");
	
	    requestUrl = helper.sign(params);
	
	    def xml = new URL(requestUrl).text
	    return new XmlSlurper().parseText(xml)
	}
	
	def getNdcProduct(q) { 
		String urlString = "http://www.HIPAASpace.com/api/ndc/getcode?q=${q.encodeAsURL()}&rt=xml&token=6BB8325D3C4F42AEBDC8F9584CA85C8D79815FA7F6194AD79793BF512981E84B"
		return getNdcResults(urlString)
	}
	/**
	 * 
	 */
	def findNdcProducts(search) {
		String q = search.searchTerms?:"";
		String urlString = "http://www.HIPAASpace.com/api/ndc/search?q=${q.encodeAsURL()}&rt=xml&token=6BB8325D3C4F42AEBDC8F9584CA85C8D79815FA7F6194AD79793BF512981E84B"
		return getNdcResults(urlString)
	}

	def getNdcResults(urlString) { 
		try {
			println "URL " + urlString
			def url = new URL(urlString)
			def connection = url.openConnection()
			if(connection.responseCode == 200){
				def xml = connection.content.text
				//println xml
				
				return processNdcProducts(xml)
				
				//search.results << product
			}
		} catch (Exception e) {
			log.error("Error trying to get products from NDC API ", e);
			throw e
		}
		return []
	}
		
	def processNdcProducts(xml) { 
		def results = []
		def ndcList = new XmlParser(false, true).parseText(xml)
		ndcList.NDC.each { ndc ->
			
			if (ndc.NDCCode) { 
				println "NDC: " + ndc
				def product = new ProductDetailsCommand()
				product.title = ndc.PackageDescription.text()
				product.ndcCode = ndc.NDCCode
				product.productType = ndc.ProductTypeName.text()
				product.packageDescription = ndc.PackageDescription.text()
				product.ndcCode = ndc.NDCCode.text()
				product.productNdcCode = ndc.ProductNDC.text()
				product.labelerName = ndc.LabelerName.text()
				product.strengthNumber = ndc.StrengthNumber.text()
				product.strengthUnit = ndc.StrengthUnit.text()
				product.pharmClasses = ndc.PharmClasses.text()
				product.dosageForm = ndc.DosageFormName.text()
				product.route = ndc.RouteName.text()
				product.proprietaryName = ndc.ProprietaryName.text()
				product.nonProprietaryName = ndc.NonProprietaryName.text()
				results << product
			}
		}
		return results;
	}
	
	/**
	 * Examples 
	 * 
	 * RxNorm
	 * 
	 * UPC Database
	 * http://www.upcdatabase.com/item/048001006812
	 * 
	 * Search UPC
	 * http://www.searchupc.com/default.aspx?q=048001006812
	 * 
	 * Google Product Search
	 * https://www.googleapis.com/shopping/search/v1/public/products?key=AIzaSyCAEGyY6QpPbm3DiHmtx6qIZ_P40FnF3vk&country=US&q=${q}&alt=scp&crowdBy=brand:1
	 * 
	 * @param q
	 * @return
	 */
	def findGoogleProducts(search) {
		def products = new ArrayList();
		int startIndex = search.startIndex;
		String q = search.searchTerms; 
		boolean spellingEnabled = search.spellingEnabled
		 
		def urlString = "https://www.googleapis.com/shopping/search/v1/public/products?" + 
			"key=AIzaSyCAEGyY6QpPbm3DiHmtx6qIZ_P40FnF3vk&country=US&q=${q.encodeAsURL()}&alt=atom&crowdBy=brand:1";
		if (startIndex > 0) { 			
			urlString += "&startIndex=" + startIndex
		}
		if (spellingEnabled) {
			urlString += "&spelling.enabled=true"
		}
		def url = new URL(urlString)
		def connection = url.openConnection()
		println "Query string = " + q + " startIndex " + startIndex
		println "URL " + urlString
		try { 
			if(connection.responseCode == 200){
				def xml = connection.content.text			
				//  <feed gd:kind="shopping#products" 
				// gd:etag="&quot;s_TKVMJ0f6e67wg989LFuFzazq0/0m3WlwDtAy5plGxzl-bgZJM-ufI&quot;" 
				// xmlns="http://www.w3.org/2005/Atom" 
				// xmlns:gd="http://schemas.google.com/g/2005" 
				// xmlns:openSearch="http://a9.com/-/spec/opensearchrss/1.0/" 
				// xmlns:s="http://www.google.com/shopping/api/schemas/2010">
	
				//def root = new XmlSlurper().parseText(blog).declareNamespace(dc: "http://purl.org/dc/elements/1.1/");
				//root.channel.item.findAll { item ->
				//	d.any{entry -> item."dc:date".text() =~ entry.key} && a.any{entry -> item.tags.text() =~ entry
				//}
	
				println "XML = \n" + xml
				
				def feed = new XmlParser(false, true).parseText(xml)
				
				def ns = new Namespace("http://www.google.com/shopping/api/schemas/2010", "s")
				def openSearch = new Namespace("http://a9.com/-/spec/opensearchrss/1.0/", "openSearch")
				
				
				search.totalResults = Integer.valueOf(feed[openSearch.totalResults].text())
				search.startIndex = Integer.valueOf(feed[openSearch.startIndex].text())
				search.itemsPerPage = Integer.valueOf(feed[openSearch.itemsPerPage].text())
				

				feed.entry.each { entry ->

					//println entry
					def product = new ProductDetailsCommand()
					
					product.link = entry[ns.product][ns.link].text()
					product.author = entry.author.name.text()
					
					
					entry.link.each { link ->						
						product.links[link.'@rel'] = link.'@href'
					}
					//println "categories: " + entry[ns.product][ns.categories][ns.category]
					
					product.id = entry.id.text()
					product.googleId = entry[ns.product][ns.googleId].text()
					// <a href='${productUrl}'></a>
					//product.category = getRootCategory();
					product.title = entry[ns.product][ns.title].text()
					product.description = entry[ns.product][ns.description].text()
					product.brand = entry[ns.product][ns.brand].text()
					//product.gtins << entry[ns.product][ns.gtin].text()
					// HACK iterates over all images, but only keeps the last one
					// Need to add these to product->documents 
					//def imageLinks = ""
					product.gtin = entry[ns.product][ns.gtin].text()
					entry[ns.product][ns.gtins][ns.gtin].each { gtin ->						
						product.gtins << gtin.text()
					}
					entry[ns.product][ns.images][ns.image].each { image ->
						product.images << image.'@link'
					}
					
					
					search.results << product
				}
				/*
				def root = new XmlSlurper().parseText(xml).
					declareNamespace(s: "http://www.google.com/shopping/api/schemas/2010")
				
				root.entry.each { entry ->
					def product = new Product()
					product.name = entry.title
					println " * " + product.name
					product.description = entry."s:product"."s:description".text()
					def link = entry."s:product"."s:link".text()
					product.description += "<br/><a href='" + link + "'>click here</a>" 
					product.manufacturer= entry."s:product"."s:brand".text()
					product.upc = entry."s:product"."s:gtin".text()
					product.manufacturer += " (" + entry."s:product"."s:author"."s:name".text() + ")"
					//println "\timages -> " + entry["s:product"]["s:images"]
					//entry["s:product"]["s:images"].each { image ->
					//	println "\timage -> " + image
					//}
					
					
					product.category = getRootCategory();
					products << product
					//result.name = geonames.geoname.name as String
					//result.lat = geonames.geoname.lat as String
					//result.lng = geonames.geoname.lng as String
					//result.state = geonames.geoname.adminCode1 as String
					//result.country = geonames.geoname.countryCode as String
				}
				*/
				
			}
			else{
				log.error(url)
				log.error(connection.responseCode)
				log.error(connection.responseMessage)
				throw new Exception(connection.responseMessage)
			}		
		} catch (Exception e) { 
			log.error("Error trying to get products from Google API ", e);
		
		}
		
	}
	
	
	/**
	* @param searchTerms
	* @param categories
	* @return
	*/
   List<Product> findProducts(List searchTerms) {
	   // Get all products, including hidden ones
	   def products = Product.list()
	   def searchResults = Product.createCriteria().list() {
		   or {
			   or {
				   searchTerms.each {
					   ilike("name", "%" + it + "%")
				   }
			   }
			   or {
				   searchTerms.each {
					   ilike("manufacturer", "%" + it + "%")
				   }
			   }
			   or {
				   searchTerms.each {
					   ilike("manufacturerCode", "%" + it + "%")
				   }
			   }
			   or {
				   searchTerms.each {
					   ilike("productCode", "%" + it + "%")
				   }
			   }
		   }
	   }
	   searchResults = products.intersect(searchResults);

	   //if (!showHiddenProducts) {
		//   searchResults.removeAll(getHiddenProducts())
	   //}

	   // now localize to only match products for the current locale
	   // TODO: this would also have to handle the category filtering
	   //  products = products.findAll { product ->
	   //  def localizedProductName = getLocalizationService().getLocalizedString(product.name);  // TODO: obviously, this would have to use the actual locale
	   // return productFilters.any {
	   //   localizedProductName.contains(it)  // TODO: this would also have to be case insensitive
	   // }
	   // }
	   return searchResults;
   }
	
	
	List<Product> getProducts(String [] ids) { 
		
		def products = []
		if (ids) { 
			products = Product.createCriteria().list() {
				'in'("id", ids)
			}
		}
		return products
	}
		
	
	Category getRootCategory() {
		def rootCategory;
		def categories = Category.findAllByParentCategoryIsNull();
		if (categories && categories.size() == 1) {
			rootCategory = categories.get(0);
		}
		else {
			rootCategory = new Category();
			rootCategory.categories = [];
			categories.each {
				rootCategory.categories << it;
			}
		}
		return rootCategory;
	}
	
	List getCategoryTree() { 
		return Category.list();		
	}
	
	List getQuickCategories() {
		List quickCategories = new ArrayList();
		String quickCategoryConfig = grailsApplication.config.inventoryBrowser.quickCategories;
		
		Category.findAll().each {
			if (it.parentCategory == null && !quickCategories.contains(it)) {
				quickCategories.add(it);
			}
		}

		if (quickCategoryConfig) {
			quickCategoryConfig.split(",").each {
				Category c = Category.findByName(it);
				if (c != null) {
					quickCategories.add(c);
				}
			};
		}
		return quickCategories;
	}
	
	
	public void validateData(ImportDataCommand command) { 
		log.info "validate data test "
		// Iterate over each row and validate values
		command?.data?.each { Map params ->
			//log.debug "Inventory item " + importParams
			log.info "validate data " + params
			//command?.data[0].newField = 'new field'
			//command?.data[0].newDate = new Date()
			params.prompts = [:]
			params.prompts["product.id"] = Product.findAllByNameLike("%" + params.search1 + "%")  
			
			//def lotNumber = (params.lotNumber) ? String.valueOf(params.lotNumber) : null;
			//if (params?.lotNumber instanceof Double) {
			//	errors.reject("Property 'Serial Number / Lot Number' with value '${lotNumber}' should be not formatted as a Double value");
			//}
			//else if (!params?.lotNumber instanceof String) {
			//	errors.reject("Property 'Serial Number / Lot Number' with value '${lotNumber}' should be formatted as a Text value");
			//}
	
	
		}

	}
	
	public void importData(ImportDataCommand command) {
		log.info "import data"

		try {
			// Iterate over each row
			command?.data?.each { Map params ->

				log.info "import data " + params
				
				/*
				// Create product if not exists
				Product product = Product.findByName(params.productDescription);
				if (!product) {
					product = new Product(params)
					product.name = params.productDescription
					//upc:params.upc,
					//ndc:params.ndc,
					//category:category,
					//manufacturer:manufacturer,
					//manufacturerCode:manufacturerCode,
					//unitOfMeasure:unitOfMeasure);

					if (!product.save()) {
						command.errors.reject("Error saving product " + product?.name)
					}
					//log.debug "Created new product " + product.name;
				}
				*/
			}

		} catch (Exception e) {
			log.error("Error importing data ", e);
			throw e;
		}

	}
	
}
