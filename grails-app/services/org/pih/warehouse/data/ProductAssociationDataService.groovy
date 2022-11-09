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

import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.Product

class ProductAssociationDataService {

    /**
     * Validate product association
     */
    Boolean validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename

        command.data.eachWithIndex { params, index ->
            params['code'] = params['code']?.toUpperCase()

            ProductAssociation productAssociationInstance = new ProductAssociation(params)
            productAssociationInstance.product = Product.findByProductCode(params['product.productCode'])
            productAssociationInstance.associatedProduct = Product.findByProductCode(params['associatedProduct.productCode'])
            params['product.id'] =  productAssociationInstance.product?.id
            params['associatedProduct.id'] =  productAssociationInstance.associatedProduct?.id

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


            ProductAssociation existingAssociation = ProductAssociation.findWhere([
                    product:            productAssociationInstance.product,
                    associatedProduct:  productAssociationInstance.associatedProduct,
                    code:               productAssociationInstance.code,
            ])

            if (existingAssociation) {
                command.errors.reject("Row ${index + 1}: association already exists")
            }
        }
    }

    void importData(ImportDataCommand command) {
        log.info "Import data " + command.filename

        command.data.each{ params ->
            ProductAssociation productAssociationInstance = new ProductAssociation(params)
            if (productAssociationInstance.validate()) {
                if (Boolean.valueOf(params.hasMutualAssociation as String) || (params.hasMutualAssociation as String)?.equalsIgnoreCase("yes")) {
                    Map otherAssociationParams = params.clone() as Map
                    otherAssociationParams['product.id'] = params['associatedProduct.id']
                    otherAssociationParams['associatedProduct.id'] = params['product.id']
                    ProductAssociation mutualAssociationInstance = new ProductAssociation(otherAssociationParams)

                    productAssociationInstance.mutualAssociation = mutualAssociationInstance
                    mutualAssociationInstance.mutualAssociation = productAssociationInstance
                    mutualAssociationInstance.save(failOnError: true)
                }
                productAssociationInstance.save(failOnError: true)
            }
        }
    }

}
