package org.pih.warehouse.product;

import org.junit.runner.Request;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductType;

import au.com.bytecode.opencsv.CSVReader;

class ProductController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"];    

    def index = {
        redirect(action: "list", params: params)
    }

	
	/**
	 * Import contents of CSV file
	 */
	def importProducts = { 
		
		if ("GET".equals(request.getMethod())) { 			
			log.info "GET request"
				render(view: "uploadProducts");
		}
		else if ("POST".equals(request.getMethod())) { 			
			log.info "POST request"
			if (!session.products) { 				
				log.info "GET request"
				
				flash.message = "Please upload a CSV file with valid products";
			}
			else { 			
				session.products.each() {
					
					
					
					new Product(name: it.name, description: it.description, productType: it.productType).save(failOnError:true);
				};
				
					
				// import 
				flash.message = "Products imported successfully"	
				redirect(controller: "product", action: "browse")
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
				CSVReader csvReader = new CSVReader(new FileReader(csvFile.getAbsolutePath()), (char) ',', (char) '\"', 1);
				while ((columns = csvReader.readNext()) != null) {
					log.info "product type: " + columns[4]
					def productType = ProductType.findByName(columns[4]);
					if (!productType) { 
						throw new Exception("Could not find Product Type with name '" + columns[4] + "'")
					}
					
					products.add(
						new Product(
							//id: columns[0],
							ean: columns[1],
							name: columns[2],
							description: columns[3],
							productType: productType));
				}
				
				session.products = products;						
				render(view: "importProducts", model: [products:products]);
			}
			else { 			
				flash.message = "Please upload a non-empty CSV file";				
			}
    	}
    }
	
    def browse = { 
    		
    	// Get selected 
		def selectedCategory = Category.get(params.categoryId);		
		def selectedConditionType = ConditionType.get(params.conditionTypeId);
		def selectedProductType = ProductType.get(params.productTypeId);
		def selectedProductSubType = ProductType.get(params.productSubTypeId);
    	def selectedAttribute = Attribute.get(params.attributeId)	
		
    	def allAttributes = Attribute.getAll();
		
    	// Condition types	
    	def allConditionTypes = ConditionType.getAll();
    	
    	// Root level product types
		def typeCriteria = ProductType.createCriteria();
		def allProductTypes = typeCriteria.list {
			isNull("parent")
		}    		

		// Root categories
		def categoryCriteria = Category.createCriteria();		
		def allCategories = categoryCriteria.list { 
			isNull("parent")
		}
				
		// Product subtypes
		def productSubtypes = null;
		if (params.productTypeId) {
			def subtypeCriteria = ProductType.createCriteria();
			productSubtypes = subtypeCriteria.list { 
				and { 
					isNotNull("parent")
					eq("parent.id", Long.parseLong(params.productTypeId))
				}
			}		
		}
				
		// Search for Products matching criteria 
		def results = null;		
		def productCriteria = Product.createCriteria();
		results = productCriteria.list {
            and{
                if(params.productTypeId && params.productTypeId != ''){
                    eq("productType.id", Long.parseLong(params.productTypeId))
                }
          		if(params.categoryId && params.categoryId != ''){
          			categories { 
          				eq("id", Long.parseLong(params.categoryId))
          			}
                }               
            }		
		}		
		
		// Search drug products matching condition type id
  		if(params.conditionTypeId && params.conditionTypeId != ''){
  			def drugProductCriteria = DrugProduct.createCriteria();
  			results = drugProductCriteria.list {  			
	  			conditionTypes { 
	  				eq("id", Long.parseLong(params.conditionTypeId))
	  			}
  			}  			
        }
		
		
		def rootCategory = new Category(name: "/", categories: allCategories);
		
        //params.max = Math.min(params.max ? params.int('max') : 10, 100)		
		render(view:'browse', model:[productInstanceList : results, 
    	                             productInstanceTotal: Product.count(), 
									 rootCategory : rootCategory,
    	                             categories : allCategories, selectedCategory : selectedCategory,
    	                             conditionTypes : allConditionTypes, selectedConditionType : selectedConditionType,
    	                             productTypes : allProductTypes, selectedProductType : selectedProductType,
    	                             productSubTypes : productSubtypes, selectedProductSubType : selectedProductSubType,
    	                             attributes : allAttributes, selectedAttribute : selectedAttribute ])
	}
    
    
    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [productInstanceList: Product.list(params), productInstanceTotal: Product.count()]
    }

    def create = {
        def productInstance = new Product()
        productInstance.properties = params
        return [productInstance: productInstance]
    }

    def save = {
        def productInstance = new Product(params)
        if (productInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'product.label', default: 'Product'), productInstance.id])}"
            redirect(action: "show", id: productInstance.id)
        }
        else {
            render(view: "create", model: [productInstance: productInstance])
        }
    }

    def show = {
        def productInstance = Product.get(params.id)
        if (!productInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(action: "list")
        }
        else {
            [productInstance: productInstance]
        }
    }

    def edit = {
        def productInstance = Product.get(params.id)
        if (!productInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [productInstance: productInstance]
        }
    }

    def update = {
        def productInstance = Product.get(params.id)
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
            if (!productInstance.hasErrors() && productInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'product.label', default: 'Product'), productInstance.id])}"
                redirect(action: "show", id: productInstance.id)
            }
            else {
                render(view: "edit", model: [productInstance: productInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def productInstance = Product.get(params.id)
        if (productInstance) {
            try {
                productInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'product.label', default: 'Product'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'product.label', default: 'Product'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'product.label', default: 'Product'), params.id])}"
            redirect(action: "list")
        }
    }
}



class ProductCommand {
	String id
	String ean
	String name
	String description
	String productType
	
	static constraints = {
	   ean(nullable: true, blank: false)
	   name(nullable: true, blank: false)
	   description(nullable:true, blank:false)
	   productType(nullable:true, blank:false)
	}
 }
