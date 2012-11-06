/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.product


import java.io.ExpiringCache.Entry;
import java.util.List;

import groovy.xml.Namespace;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Tag;
import org.pih.warehouse.importer.ImportDataCommand;
import org.pih.warehouse.core.ApiException;


/**
 * Keys
 * 
 * @author jmiranda
 *
 */
class ProductService {
	
	def grailsApplication
	
	
	def getAllTags() { 
		return Tag.list().collect { it.tag }.unique()		
	}
	
	/**
	 * 	
	 * @param query
	 * @return
	 */
	def getNdcProduct(query) { 
		return getNdcResults("getcode", query)
	}
	
	/**
	 * 
	 */
	def findNdcProducts(search) {
		return getNdcResults("search", search.searchTerms?:"")
	}

	def getNdcResults(operation, q) { 
		def hipaaspaceApiKey = grailsApplication.config.hipaaspace.api.key
		if (!hipaaspaceApiKey) {
			throw new ApiException(message: "Your administrator must specify Hipaaspace.com API key (hipaaspace.api.key) in configuration file (openboxes-config.properties).  Sign up at <a href='http://www.hipaaspace.com/myaccount/login.aspx?ReturnUrl=%2fmyaccount%2fdefault.aspx' target='_blank'>hipaaspace.com</a>.")
		}		
		try {
			def url = new URL("http://www.HIPAASpace.com/api/ndc/search?q=${q?.encodeAsURL()}&rt=xml&key=${hipaaspaceApiKey}")
			def connection = url.openConnection()
			if(connection.responseCode == 200){
				def xml = connection.content.text
				//println xml
				
				return processNdcProducts(xml)
				
				//search.results << product
			}
		} catch (Exception e) {
			log.error("Error trying to get products from NDC API ", e);
			throw new ApiException(message: "Unable to query NDC database: " + e.message)
		}
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
	 *	<rxnormdata>
	 * 		<displayTermsList>
	 * 			<term></term>
	 * 		</displayTermsList>
	 * 	</rxnormdata>	
	 * @return
	 */
	
	def findRxNormDisplayNames() { 
		String url = "http://rxnav.nlm.nih.gov/REST/displaynames"
		return processXml(url, "displayTermsList.term")		
	}
	
	def processXml(urlString, itemName) {
		try {
			def results = []
			println "URL " + urlString
			def url = new URL(urlString)
			def connection = url.openConnection()
			if (connection.responseCode == 200) {
				def xml = connection.content.text
				
				def list = new XmlParser(false, true).parseText(xml)
				for (item in list.displayTermsList.term) { 
					println "item: " + item.class.name
					results << item.text()
				}
				return results
			}
		} catch (Exception e) {
			log.error("Error trying to get products from NDC API ", e);
			throw e
		}
	}
	
	/**
	 * @param q
	 * @return
	 */
	def findGoogleProducts(search) {
		
		def googleProductSearchKey = grailsApplication.config.google.api.key
		if (!googleProductSearchKey) { 
			throw new ApiException(message: "Your administrator must specify Google API key (google.api.key) in configuration file (openboxes-config.properties).  For more information, see Google's <a href='https://developers.google.com/shopping-search/v1/getting_started#getting-started' target='_blank'>Getting Started</a> guide")
		}
		def products = new ArrayList();
		int startIndex = search.startIndex;
		String q = search.searchTerms; 
		boolean spellingEnabled = search.spellingEnabled
		 
		def urlString = "https://www.googleapis.com/shopping/search/v1/public/products?" + 
			"key=${googleProductSearchKey}&country=US&q=${q.encodeAsURL()}&alt=atom&crowdBy=brand:1";
		if (startIndex > 0) { 			
			urlString += "&startIndex=" + startIndex
		}
		if (spellingEnabled) {
			urlString += "&spelling.enabled=true"
		}
		def url = new URL(urlString)
		def connection = url.openConnection()
		if(connection.responseCode == 200){
			def xml = connection.content.text			
			def feed = new XmlParser(false, true).parseText(xml)
			
			def ns = new Namespace("http://www.google.com/shopping/api/schemas/2010", "s")
			def openSearch = new Namespace("http://a9.com/-/spec/opensearchrss/1.0/", "openSearch")

			search.totalResults = Integer.valueOf(feed[openSearch.totalResults].text())
			search.startIndex = Integer.valueOf(feed[openSearch.startIndex].text())
			search.itemsPerPage = Integer.valueOf(feed[openSearch.itemsPerPage].text())
			
			feed.entry.each { entry ->

				def product = new ProductDetailsCommand()
				
				product.link = entry[ns.product][ns.link].text()
				product.author = entry.author.name.text()
				
				
				entry.link.each { link ->						
					product.links[link.'@rel'] = link.'@href'
				}
				//println "categories: " + entry[ns.product][ns.categories][ns.category]
				
				product.id = entry.id.text()
				product.googleId = entry[ns.product][ns.googleId].text()
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
		}
		else {			
			log.info("URL: " + url)
			log.info("Response Code: " + connection.responseCode)
			log.info("Response Message: " + connection.responseMessage)
			//log.info("Response: " + connection.content)
			throw new ApiException("Unable to connect to Google Product Search API using connection URL [" + urlString + "]: " + connection.responseMessage)
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
