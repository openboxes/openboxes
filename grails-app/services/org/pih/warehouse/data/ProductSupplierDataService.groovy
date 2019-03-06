/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.data

import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.springframework.validation.BeanPropertyBindingResult

class ProductSupplierDataService {

    //boolean transactional = true

    Boolean validate(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        command.data.eachWithIndex { params, index ->

            if(params.id && !ProductSupplier.exists(params["code"])) {
                command.errors.reject("Row ${index+1}: Product supplier with ID ${params.id} does not exist")
            }

            if (!Product.findByProductCode(params["product.productCode"])) {
                command.errors.reject("Row ${index+1}: Product with productCode ${params['product.productCode']} does not exist")
            }

            def supplier = Organization.get(params["supplier.id"])
            if(supplier?.name != params["supplier.name"]) {
                command.errors.reject("Row ${index+1}: Organization ${supplier.name} with id ${params['supplier.id']} does not match ${params['supplier.name']}")
            }

            def manufacturer = Organization.get(params["manufacturer.id"])
            if(manufacturer?.name != params["manufacturer.name"]) {
                command.errors.reject("Row ${index+1}: Organization ${manufacturer.name} with id ${params['manufacturer.id']} does not match ${params['manufacturer.name']}")
            }

            def productSupplier = createOrUpdate(params)
            if (!productSupplier.validate()) {
                productSupplier.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("Row ${index+1}: ${error.getFieldError()}")
                }
            }
        }
    }

    void process(ImportDataCommand command) {
        log.info "Process data " + command.filename

        command.data.eachWithIndex { params, index ->
            ProductSupplier productSupplier = createOrUpdate(params)
            if (productSupplier.validate()) {
                productSupplier.save(failOnError: true)
            }
        }
    }

    def createOrUpdate(Map params) {
        ProductSupplier productSupplier = ProductSupplier.get(params["code"])
        if (!productSupplier) {
            productSupplier = new ProductSupplier(params)
        }
        else {
            productSupplier.properties = params
        }

        productSupplier.product = Product.findByProductCode(params["product.productCode"])
        productSupplier.supplier = Organization.get(params["supplier.id"])
        productSupplier.manufacturer = Organization.get(params["manufacturer.id"])

        return productSupplier
    }
}
