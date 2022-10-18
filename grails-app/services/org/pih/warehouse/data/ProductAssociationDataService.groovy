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
import org.springframework.validation.BeanPropertyBindingResult


class ProductAssociationDataService {

    /**
     * Validate product association
     */
    Boolean validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename

        command.data.eachWithIndex { params, index ->
            ProductAssociation productAssociationInstance = new ProductAssociation(params)

            // disable update product association on import
            if (productAssociationInstance?.id) {
                command.errors.reject("Row ${index + 1}: Cannot edit existing associations via import")
                return
            }

            Product product = Product.get(productAssociationInstance.product?.id)
            Product associatedProduct = Product.get(productAssociationInstance.associatedProduct?.id)

            if (!product) {
                command.errors.reject(
                    "Row ${index + 1}: Product with code '${productAssociationInstance.product?.id}' does not exist"
                )
                return
            }
            if (!associatedProduct) {
                command.errors.reject(
                    "Row ${index + 1}: Product with code '${productAssociationInstance.associatedProduct?.id}' does not exist"
                )
                return
            }
            if (!product.active) {
                command.errors.reject(
                    "Row ${index + 1}: Product with code '${productAssociationInstance.product?.id}' is inactive"
                )
                return
            }
            if (!associatedProduct.active) {
                command.errors.reject(
                    "Row ${index + 1}: Product with code '${productAssociationInstance.associatedProduct?.id}' is inactive"
                )
                return
            }
            if (product == associatedProduct) {
                command.errors.reject("Cannot associate a product with itself")
                return
            }

            // Check for association duplicates
            List<ProductAssociation> foundProductAssociations = ProductAssociation.findAllWhere([
                    product             : productAssociationInstance.product,
                    associatedProduct   : productAssociationInstance.associatedProduct,
                    code                : productAssociationInstance.code,
            ])
            if (foundProductAssociations && foundProductAssociations.size() > 0) {
                command.errors.reject("Row ${index + 1}: Association already exists")
                return
            }


            // find two-way association pairs
            def twoWayAssociationPair = command.data.find {
                it['product.id'] == params['associatedProduct.id'] &&
                it['associatedProduct.id'] == params['product.id'] &&
                it['code'] == params['code']
            }

            if (twoWayAssociationPair) {
                // mark this association as two-way association
                params.hasMutualAssociation = true
                if (twoWayAssociationPair['quantity'] * productAssociationInstance.quantity != 1) {
                    command.errors.reject(
                            "Row ${index + 1}: Quantity of Product association for Products with codes " +
                            "'${productAssociationInstance.product?.id}' and " +
                            "'${twoWayAssociationPair['associatedProduct.id']}' does not match"
                    )
                    return
                }
            }

            if (!productAssociationInstance.validate()) {
                productAssociationInstance.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject(
                            "Row ${index + 1}: Product Association with Product code " +
                            "'${productAssociationInstance.product?.id}': ${error.getFieldError()}"
                    )
                }
            }
        }
    }

    void importData(ImportDataCommand command) {
        log.info "Import data " + command.filename

        def individualAssociations = command.data.findAll{ !it.hasMutualAssociation }
        def twoWayAssociations = command.data
                .findAll{ it.hasMutualAssociation }
                .groupBy { [it['product.id'], it['associatedProduct.id']] as Set<String> }

        individualAssociations.each { params ->
            ProductAssociation productAssociationInstance = new ProductAssociation(params)
            productAssociationInstance.save(failOnError: true)
        }

        twoWayAssociations.each { key, paramList ->
            def firstProductParams = paramList[0]
            def secondProductParams = paramList[1]
            ProductAssociation firstProductAssociationInstance = new ProductAssociation(firstProductParams)
            ProductAssociation secondProductAssociationInstance = new ProductAssociation(secondProductParams)

            firstProductAssociationInstance.mutualAssociation = secondProductAssociationInstance
            secondProductAssociationInstance.mutualAssociation = firstProductAssociationInstance

            firstProductAssociationInstance.save(failOnError: true)
            secondProductAssociationInstance.save(failOnError: true)
        }
    }

}
