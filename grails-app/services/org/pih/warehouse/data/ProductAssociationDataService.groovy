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

import grails.validation.ValidationException
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.Product

class ProductAssociationDataService {

    /**
     * Validate product association
     */
    Boolean validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        List<Map> listOfValidatedProductAssociationParams = []

        command.data.eachWithIndex { params, index ->
            params['code'] = params['code']?.toUpperCase()
            params['quantity'] = params['conversion'] ?: 1

            ProductAssociation productAssociationInstance = new ProductAssociation(params)
            productAssociationInstance.product = Product.findByProductCode(params['product.productCode'])
            productAssociationInstance.associatedProduct = Product.findByProductCode(params['associatedProduct.productCode'])
            params['product'] =  productAssociationInstance.product?.id
            params['product.name'] =  productAssociationInstance.product?.name
            params['associatedProduct'] =  productAssociationInstance.associatedProduct?.id
            params['associatedProduct.name'] =  productAssociationInstance.associatedProduct?.name

            params.hasMutualAssociation = Boolean.valueOf(params.hasMutualAssociation as String) || (params.hasMutualAssociation as String)?.equalsIgnoreCase("yes")

            // check for duplicate association entries in the file
            def indexOfDuplicate = listOfValidatedProductAssociationParams.findIndexOf{ it ->
                // When dealing with two-way associations we need to check for existence of "the other side" of the two-way association
                // For example - when we are importing below associations
                // 1. ( product: A, associatedProduct: B, code: AA, hasMutualAssociation: false )
                // 2. ( product: B, associatedProduct: A, code: AA, hasMutualAssociation: true )
                // By importing the 2-nd entry it creates two separate associations
                // So we need to make sure that none of those associations will collide with one another (like 1-st entry)
                if (params.hasMutualAssociation || it.hasMutualAssociation) {
                    return  (
                            params['product'] == it['product'] &&
                            params['associatedProduct'] == it['associatedProduct'] &&
                            params['code'] == it['code']
                        ) || (
                            params['product'] == it['associatedProduct'] &&
                            params['associatedProduct'] == it['product'] &&
                            params['code'] == it['code']
                        )
                }
                // if it is not a mutual association we can check for exact duplicate
                return params['product'] == it['product'] &&
                       params['associatedProduct'] == it['associatedProduct'] &&
                       params['code'] == it['code']
            }
            if (indexOfDuplicate >= 0) {
                command.errors.reject("Row ${index + 1}: Duplicate association on row: ${indexOfDuplicate + 1}")
            }
            // disable update product association on import
            if (params['id']) {
                command.errors.reject("Row ${index + 1}: Cannot edit existing associations via import")
            }

            if (!params['code']) {
                command.errors.reject("Row ${index + 1}: Association Type field can not be empty")
            } else if (!productAssociationInstance?.code) {
                command.errors.reject("Row ${index + 1}: Association Type code '${params['code']}' does not exist")
            }

            if (!productAssociationInstance.product) {
                command.errors.reject(
                    "Row ${index + 1}: Product with code '${params['product.productCode']}' does not exist"
                )
            }
            if (!productAssociationInstance.associatedProduct) {
                command.errors.reject(
                    "Row ${index + 1}: Product with code '${params['associatedProduct.productCode']}' does not exist"
                )
            }
            if (productAssociationInstance.product && !productAssociationInstance.product.active) {
                command.errors.reject(
                    "Row ${index + 1}: Product with code '${productAssociationInstance.product?.productCode}' is inactive"
                )
            }
            if (productAssociationInstance.associatedProduct && !productAssociationInstance.associatedProduct.active) {
                command.errors.reject(
                    "Row ${index + 1}: Product with code '${productAssociationInstance.associatedProduct?.productCode}' is inactive"
                )
            }
            if (productAssociationInstance.product?.id == productAssociationInstance.associatedProduct?.id) {
                command.errors.reject("Cannot associate a product with itself")
            }

            // Forbid user from importing a two-way association if one-way for this association already exists
            // Case explanation presented few lines above
            if (params.hasMutualAssociation) {
                List<ProductAssociation> existingAssociations = ProductAssociation.createCriteria().list {
                    or {
                        and {
                            eq("product", productAssociationInstance.product)
                            eq("associatedProduct", productAssociationInstance.associatedProduct)
                            eq("code", productAssociationInstance.code)
                        }
                        and {
                            eq("product", productAssociationInstance.associatedProduct)
                            eq("associatedProduct", productAssociationInstance.product)
                            eq("code", productAssociationInstance.code)
                        }
                    }
                }
                if (existingAssociations) {
                    if (existingAssociations.any{ it.mutualAssociation }) {
                        command.errors.reject("Row ${index + 1}: Same two-way association already exists")
                    } else {
                        command.errors.reject("Row ${index + 1}: One-way of the association already exists")
                    }
                }
            } else {
                // if it is not a mutual association we can check for exact duplicate
                ProductAssociation existingAssociation = ProductAssociation.findWhere([
                        product:            productAssociationInstance.product,
                        associatedProduct:  productAssociationInstance.associatedProduct,
                        code:               productAssociationInstance.code,
                ])

                if (existingAssociation) {
                    command.errors.reject("Row ${index + 1}: association already exists")
                }
            }
            // add visited/validated params to the list to check for duplicates
            listOfValidatedProductAssociationParams.add(params)
        }
    }

    void importData(ImportDataCommand command) {
        log.info "Import data " + command.filename

        command.data.each{ params ->
            ProductAssociation productAssociationInstance = new ProductAssociation(params)

            if (params.hasMutualAssociation) {
                Map otherAssociationParams = params.clone() as Map
                otherAssociationParams['product'] = params['associatedProduct']
                otherAssociationParams['associatedProduct'] = params['product']
                ProductAssociation mutualAssociationInstance = new ProductAssociation(otherAssociationParams)

                productAssociationInstance.mutualAssociation = mutualAssociationInstance
                mutualAssociationInstance.mutualAssociation = productAssociationInstance
            }

            if (!productAssociationInstance.validate() || !productAssociationInstance.save(failOnError: true, flush: true)) {
                throw new ValidationException("Invalid product association", productAssociationInstance.errors)
            }
        }
    }
}
