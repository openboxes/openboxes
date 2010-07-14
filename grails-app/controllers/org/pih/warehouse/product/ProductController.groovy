package org.pih.warehouse.product;

import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductType;

class ProductController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"];    

    def index = {
        redirect(action: "list", params: params)
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
    	                             categories : allCategories,
    	                             conditionTypes : allConditionTypes,
    	                             productTypes : allProductTypes, 
    	                             productSubTypes : productSubtypes,
    	                             attributes : allAttributes,
    	                             selectedAttribute : selectedAttribute,
    	                            
    	                             selectedCategory : selectedCategory,
    	                             selectedConditionType : selectedConditionType,
    	                             selectedProductType : selectedProductType,
    	                             selectedProductSubType : selectedProductSubType])
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
