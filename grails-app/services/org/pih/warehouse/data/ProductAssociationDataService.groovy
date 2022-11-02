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
            params['quantity'] = params['conversion'] ?: 1

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

            // Check for association duplicates
            List<ProductAssociation> foundProductAssociations = ProductAssociation.findAllWhere([
                    product             : productAssociationInstance.product,
                    associatedProduct   : productAssociationInstance.associatedProduct,
                    code                : productAssociationInstance.code,
            ])
            if (!params['id'] && foundProductAssociations && foundProductAssociations.size() > 0) {
                command.errors.reject("Row ${index + 1}: Association already exists")
            }


            // find another product association that matches current associationInstance
            // for two-way association relationship, where:
            // - association.product = otherAssociation.associatedProduct
            // - association.associatedProduct = otherAssociation.product
            // - association.code = otherAssociation.code
            def otherMatchingTwoWayAssociation = command.data.find {
                it['product.productCode'] == params['associatedProduct.productCode'] &&
                it['associatedProduct.productCode'] == params['product.productCode'] &&
                it['code'] == params['code']
            }

            if (otherMatchingTwoWayAssociation) {
                // if such a product association exists then
                // mark current association as two-way association
                // and validate quantity on both product associations
                params.hasMutualAssociation = true
                if (otherMatchingTwoWayAssociation['quantity'] * productAssociationInstance.quantity != 1) {
                    command.errors.reject(
                            "Row ${index + 1}: Quantity of Product association for Products with codes " +
                            "'${productAssociationInstance.product?.productCode}' and " +
                            "'${otherMatchingTwoWayAssociation['associatedProduct.productCode']}' does not match"
                    )
                }
            }
        }
    }

    void importData(ImportDataCommand command) {
        log.info "Import data " + command.filename

        def individualAssociations = command.data.findAll{ !it.hasMutualAssociation }
        // map of two-way product associations grouped by a product and associatedProduct pairs
        def twoWayAssociations = command.data
                .findAll{ it.hasMutualAssociation }
                .groupBy { [it['product.productCode'], it['associatedProduct.productCode']] as Set<String> }
        // create individual associations that are not bound by two-way association relationship
        individualAssociations.each { params ->
            ProductAssociation productAssociationInstance = new ProductAssociation(params)
            if (productAssociationInstance.validate()) {
                productAssociationInstance.save(failOnError: true)
            }
        }
        // two way association is a pair of two associations where:
        // - association1.product == association2.associatedProduct
        // - association1.associatedProduct == association2.product
        // - association1.code == association2.code
        // - association1.quantity == 1 / association2.quantity
        twoWayAssociations.each { key, paramList ->
            def firstAssociationParams = paramList[0]
            def secondAssociationParams = paramList[1]
            ProductAssociation firstProductAssociationInstance = new ProductAssociation(firstAssociationParams)
            ProductAssociation secondProductAssociationInstance = new ProductAssociation(secondAssociationParams)

            firstProductAssociationInstance.mutualAssociation = secondProductAssociationInstance
            secondProductAssociationInstance.mutualAssociation = firstProductAssociationInstance

            if (firstProductAssociationInstance.validate() && secondProductAssociationInstance.validate()) {
                firstProductAssociationInstance.save(failOnError: true)
                secondProductAssociationInstance.save(failOnError: true)
            }
        }
    }

}
