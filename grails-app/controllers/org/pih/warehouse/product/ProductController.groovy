package org.pih.warehouse.product;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.junit.runner.Request;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.inventory.InventoryItem;
import grails.converters.JSON;

import au.com.bytecode.opencsv.CSVReader;

class ProductController {

	def inventoryService;
	def productService;

    static allowedMethods = [save: "POST", update: "POST"];    
	
	
    def index = {
        redirect(action: "list", params: params)
    }

	//params.max = Math.min(params.max ? params.int('max') : 10, 100)
    //    [eventTypeInstanceList: EventType.list(params), eventTypeInstanceTotal: EventType.count()]

	def removeCategoryFilter = {
		
		def category = Category.get(params?.categoryId)
		if (category)
			session.productCategoryFilters.remove(category?.id);
			
		redirect(action: browse);
	}
	
	def clearCategoryFilters = {
		session.productCategoryFilters.clear();
		session.productCategoryFilters = null;
		redirect(action: browse);
	}
	
	def addCategoryFilter = {
		def category = Category.get(params?.categoryId);
		if (category && !session.productCategoryFilters.contains(category?.id))
			session.productCategoryFilters << category?.id;
		redirect(action: browse);
	}
	
	
    def browse = { 
		//params.max = Math.min(params.max ? params.int('max') : 10, 100);

		// Hydrate the category filters from the session
		// Allow us to get any attribute of a category without get a lazy init exception
		def categoryFilters = []
		if (session.productCategoryFilters) {
			session.productCategoryFilters.each {
				categoryFilters << Category.get(it);
			}
		}
		
		// Get all products in the given categories.  If there are no products returned, 
		// we should to display all products by default.
		def products = inventoryService.getProductsByCategories(categoryFilters);		
		products = products ?: Product.list();
		def productsByCategory = products.groupBy { it.category }
		
		render(view:'browse', model:[productInstanceList : products, 
    	                             productInstanceTotal: products.size(), 
									 productsByCategory : productsByCategory,
									 rootCategory : productService.getRootCategory(),
									 categoryFilters: categoryFilters ])
	}
    
	
	/** 
	 * Perform a bulk update of 
	 */
	def batchEdit = { BatchEditCommand cmd -> 
		
		log.info "Batch edit: " + params
		
		cmd.productInstanceList = Product.getAll();
		cmd.productInstanceList.eachWithIndex { product, index ->
			println product.category
			cmd.categoryInstanceList << product.category;
		}		
		cmd.rootCategory = productService.getRootCategory();
		
		[ commandInstance : cmd ]
		
	}
	
	def batchSave = { BatchEditCommand cmd ->
		
		// If there are no products (usually when returning to batchSave after login
		if (!cmd.productInstanceList) { 
			
			redirect(action: 'batchEdit')	
		}
		// We needed to hack the category binding in order to make this work.
		// When changing the product.category directly, we received an error 
		// from Hibernate stating that we were trying to change the primary key
		// of the category object.
		cmd.categoryInstanceList.eachWithIndex { cat, i ->
			log.info "categoryInstanceList[" + i + "]: " + cat;
			cmd.productInstanceList[i].category = Category.get(cat.id);
		}

		cmd.productInstanceList.eachWithIndex { prod, i ->
			log.info "productInstanceList[" + i + "]: " + prod.category;
			if (!prod.hasErrors() && prod.save()) { 
				// saved with no errors
			}
			else { 
				// copy the errors from this product on to the overall command object errors
				prod.errors.getAllErrors(). each {
					cmd.errors.reject(it.getCode(), it.getDefaultMessage())
				}
			}
		}

		if (!cmd.hasErrors()) { 
			flash.message = "All products were saved successfully"
		}
		else {
			// reset the flash message in the case of two submits in a row
			flash.message = null
		}
		
		
		cmd.rootCategory = productService.getRootCategory();
		
		render(view: "batchEdit", model: [commandInstance:cmd]);
				
	}
	
	
    
    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [productInstanceList: Product.list(params), productInstanceTotal: Product.count()]
    }
	
	def create = { 
		def productInstance = new Product(params)
		render(view: "edit", model: [productInstance : productInstance, rootCategory: productService.getRootCategory()])
	}

	
    def save = {
        //def productInstance = new Product(params)
		log.info "save called with params " + params
		log.info "type = " + params.type;
		
		def productInstance = new Product();	
		/*
		productInstance?.categories?.clear();
		println "size: " + productInstance?.categories?.size()
		params.each {
			println ("category: " + it.key +  " starts with category_ " + it.key.startsWith("category_"))			
			if (it.key.startsWith("category_")) {
				def category = Category.get((it.key - "category_") as Integer);
				log.info "adding " + category?.name
				productInstance.addToCategories(category)
			}
		  }
		*/
		productInstance.properties = params
		
		log.info("Categories " + productInstance?.categories);
		
		// find the phones that are marked for deletion
		def _toBeDeleted = productInstance.categories.findAll {(it?.deleted || (it == null))}
		
		log.info("toBeDeleted: " + _toBeDeleted )
		
		// if there are phones to be deleted remove them all
		if (_toBeDeleted) {
			productInstance.categories.removeAll(_toBeDeleted)
		}
		
		
		if (!productInstance.hasErrors() && productInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'product.label', default: 'Product'), productInstance.name])}"
			
            redirect(action: "browse", params:params)
        }
        else {
            render(view: "edit", model: [productInstance: productInstance, rootCategory: productService.getRootCategory()])
        }
    }

    def show = {
        def productInstance = Product.get(params.id)
        if (!productInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(action: "browse")
        }
        else {
            [productInstance: productInstance]
        }
    }

    def edit = {
        def productInstance = Product.get(params.id)
        if (!productInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(action: "browse")
        }
        else {
            return [productInstance: productInstance, rootCategory: productService.getRootCategory()]
        }
    }
	
    def update = {
		
		log.info "update called with params " + params
        def productInstance = Product.get(params.id)
		log.info " " + productInstance.class.simpleName
		
        if (productInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (productInstance.version > version) {                    
                    productInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'product.label', default: 'Product')] as Object[], "Another user has updated this Product while you were editing")
                    render(view: "edit", model: [productInstance: productInstance])
                    return
                }
            }
            productInstance.properties = params
			
			log.info("Categories " + productInstance?.categories);
			
			// find the phones that are marked for deletion
			def _toBeDeleted = productInstance.categories.findAll {(it?.deleted || (it == null))}
			
			log.info("toBeDeleted: " + _toBeDeleted )
			
			// if there are phones to be deleted remove them all
			if (_toBeDeleted) {
				productInstance.categories.removeAll(_toBeDeleted)
			}
			 
			//update my indexes
			//productInstance.categories.eachWithIndex(){cat, i ->
			//	cat.index = i
			//}
			
			/*
			productInstance?.categories?.clear();		
			params.each {
				println ("category: " + it.key +  " starts with category_ " + it.key.startsWith("category_"))
				
				if (it.key.startsWith("category_")) {
					def category = Category.get((it.key - "category_") as Integer);
					log.info "adding " + category?.name
					productInstance.addToCategories(category)
				}
			}
			*/
            if (!productInstance.hasErrors() && productInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'product.label', default: 'Product'), productInstance.name])}"
                redirect(action: "browse", params:params)
            }
            else {
                render(view: "edit", model: [productInstance: productInstance, rootCategory: productService.getRootCategory()])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(action: "browse", params:params)
        }
    }

    def delete = {
    	def productInstance = Product.get(params.id)
        if (productInstance && !productInstance.hasAssociatedTransactionEntriesOrShipmentItems()) {	        	
	          try {
	        	  // first we need to delete any inventory items associated with this transaction
	        	def items = InventoryItem.findAllByProduct(productInstance)
	        	items.each { it.delete(flush:true) }
	        	  	
	        	// now delete the actual product
	            productInstance.delete(flush: true)
	            
	            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'product.label', default: 'Product'), params.id])}"
	            redirect(action: "browse")
		      }
		      catch (org.springframework.dao.DataIntegrityViolationException e) {
	            flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'product.label', default: 'Product'), params.id])}"
	            redirect(action: "edit", id: params.id)
		      }
        }
        else {
            flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(action: "edit", id: params.id)
        }
    }
	
	/**
	 * 
	 */
	def importDependencies = { 		
		/*
		if (session.dosageForms) {
			session.dosageForms.unique().each() {
				new DosageForm(code: it, name: it).save(flush:true)
			}
			session.dosageForms = null
		}*/
				
		redirect(controller: "product", action: "importProducts")	
	}
	
	/**
	* Import contents of CSV file
	*/
   def importProducts = {
	   
	   if ("GET".equals(request.getMethod())) {
		   render(view: "uploadProducts");
	   }
	   else if ("POST".equals(request.getMethod())) {
		   log.info "POST request"
		   if (!session.products) {
			   log.info "GET request"
			   
			   flash.message = "Please upload a CSV file with valid products";
		   }
		   else {			   
			   
			   if (!session.dosageForms && !session.productTypes) { 
				   session.products.each() {					   
					   def productInstance = new Product(	name: it.name, 
															productCode: it.productCode);						
						productInstance.save(failOnError:true);
					};
				   // import
				   flash.message = "Products imported successfully"
				   redirect(controller: "product", action: "browse")
			   }			   								   
			   else { 
				   flash.message = "Please import dependencies first"
				   redirect(controller: "product", action: "importProducts")				   
			   }
		   }
	   }
   }


	   
   /**
	* Upload and process CSV file
	*/
   def uploadProducts = {
	   
	   if ("POST".equals(request.getMethod())) {
		   
		   
		   def uploadFile = request.getFile('csvFile');
		   
		   // file must be less than 1MB
		   if (!uploadFile?.empty) {
			   File csvFile = new File("/tmp/warehouse/products/import/" + uploadFile.originalFilename);
			   csvFile.mkdirs()
			   
			   uploadFile.transferTo(csvFile);
			   
			   
			   //def sql = Sql.newInstance("jdbc:mysql://localhost:3306/mydb", "user", "pswd", "com.mysql.jdbc.Driver")
			   //def people = sql.dataSet("PERSON")
			   List<Product> products = new ArrayList<Product>();
			   
			   /*
			   csvFile.splitEachLine(",") { fields ->
				   log.info("field0: " + fields[0])
				   log.info("field1: " + fields[1])
				   log.info("field2: " + fields[2])
				   
				   products.add(
					   new ProductCommand(
						   id: fields[0],
						   ean: fields[1],
						   name: fields[2],
						   description: fields[3],
						   productType: fields[4]));
			   }*/

			   
				// Process CSV file
				def columns;
				def productTypes = new HashSet<String>();
				def categories = []
				def unitOfMeasures = []
				def dosageForms = new HashSet<String>();
				
				
				CSVReader csvReader = new CSVReader(new FileReader(csvFile.getAbsolutePath()), (char) ',', (char) '\"', 1);
				while ((columns = csvReader.readNext()) != null) {
					
					// 0 => type
					// 1 => productType
					// 2 => productCode
					// 3 => name
					// 4 => frenchName
					// 5 => dosageStrength
					// 6 => unitOfMeasure
					// 7 => dosageForm
					
					def productInstance = new Product();
					productInstance.productCode = columns[2]
					productInstance.name = columns[3]
					productInstance.frenchName = columns[4]
					productInstance.dosageStrength = columns[5]
					productInstance.dosageUnit = columns[6]
					
					/*
					def productTypeValue = columns[1];
					if (productTypeValue && !productTypeValue.equals("") && !productTypeValue.equals("null")) { 
						def productType = ProductType.findByName(productTypeValue);
						if (!productType) { 
							productInstance.productType = new ProductType(name: productTypeValue, code: productTypeValue);						
							productTypes.add(productTypeValue);
						}else { 
							productInstance.productType = productType
						}
					}					
					
					def dosageFormValue = columns[7];
					if (dosageFormValue && !dosageFormValue.equals("") && !dosageFormValue.equals("null")) { 					
						def dosageForm = DosageForm.findByName(dosageFormValue);					
						if (!dosageForm) { 
							productInstance.dosageForm = new DosageForm(name: dosageFormValue, code: dosageFormValue)
							dosageForms.add(dosageFormValue);
						}
						else { 
							productInstance.dosageForm = dosageForm
						}
					}
					*/
					
											
					products.add(productInstance);

			   }
			   
			   
			   
			   session.products = products;
			   //session.categories = categories;
			   session.productTypes = productTypes;
			   session.dosageForms = dosageForms;
			   
			   if (dosageForms || productTypes) { 
				   flash.message = "Please import all dependencies first"
				   render(view: "importProducts", model: [productTypes: productTypes, dosageForms: dosageForms]);
			   }
			   else { 
				   render(view: "importProducts", model: [products:products]);				   
			   }
		   }
		   else {
			   flash.message = "Please upload a non-empty CSV file";
		   }
	   }
   }
	
	
}




