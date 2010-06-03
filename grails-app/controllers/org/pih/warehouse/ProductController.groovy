package org.pih.warehouse

import org.pih.warehouse.api.AuthService;
import org.pih.warehouse.ProductType;

class ProductController {

    AuthService authService;
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"];
    
    def beforeInterceptor = [action:this.&auth,except:'login']
    
    // defined as a regular method so its private
    def auth() {
    	/*
		println "checking if user is authenticated $session.user";
		if(!session.user) {
		    println "user in not authenticated";
		    redirect(controller: "auth", action: "login");
		    return false
		} else {
		    println "user is authenticated";
		}*/
    }

    def login = {
	
    }

    def index = {
        redirect(action: "list", params: params)
    }

    def browse = { 

    	// Condition types	
    	def allConditionTypes = ConditionType.getAll();
    	
    	// Product types
		def typeCriteria = ProductType.createCriteria();
		def allProductTypes = typeCriteria.list {
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
		
		def selectedCategory = Category.get(params.categoryId);		
		def selectedConditionType = ConditionType.get(params.conditionTypeId);
		def selectedProductType = ProductType.get(params.productTypeId);
		def selectedProductSubType = ProductType.get(params.productSubTypeId);

		
		// Search for Products matching criteria 
		def results = null;		
		def productCriteria = Product.createCriteria();
		results = productCriteria.list {
            and{
                if(params.productTypeId && params.productTypeId != ''){
                    eq("type.id", Long.parseLong(params.productTypeId))
                }
                if(params.productSubTypeId && params.productSubTypeId != ''){
                    eq("subType.id", Long.parseLong(params.productSubTypeId))
                }                
          		if(params.conditionTypeId && params.conditionTypeId != ''){
          			conditionTypes { 
          				eq("id", Long.parseLong(params.conditionTypeId))
          			}
                }
          		if(params.categoryId && params.categoryId != ''){
          			categories { 
          				eq("id", Long.parseLong(params.categoryId))
          			}
                }
               
            }
		
		}		
		
        //params.max = Math.min(params.max ? params.int('max') : 10, 100)		
		render(view:'browse', model:[productInstanceList : results, 
    	                             productInstanceTotal: Product.count(), 
    	                             conditionTypes : allConditionTypes,
    	                             productTypes : allProductTypes, 
    	                             productSubTypes : productSubtypes,
    	                             
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
