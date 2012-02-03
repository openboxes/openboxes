package org.pih.warehouse.product

import org.pih.warehouse.importer.ImportDataCommand;

class ProductService {
	
	def grailsApplication

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
		if (quickCategoryConfig) {
			quickCategoryConfig.split(",").each {
				Category c = Category.findByName(it);
				if (c != null) {
					quickCategories.add(c);
				}
			};
		}
		Category.findAll().each {
			if (it.parentCategory == null && !quickCategories.contains(it)) {
				quickCategories.add(it);
			}
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
