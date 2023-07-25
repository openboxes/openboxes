/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.product

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional

@Transactional
class ProductGroupService {

    GrailsApplication grailsApplication

    def addProductToProductGroup(String productGroupId, String productId, boolean isProductFamily) {
        def g = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
        ProductGroup productGroup = ProductGroup.get(productGroupId)
        Product product = Product.get(productId)
        if (productGroup && product) {
            // If we are adding product family to product and it already has one, throw an exception
            if (isProductFamily && product.productFamily) {
                throw new IllegalArgumentException(g.message(code: "productGroup.productFamily.duplicate.error", default: "This product already has a product family"))
            }
            isProductFamily ? productGroup.addToSiblings(product) : productGroup.addToProducts(product)
        }
        return productGroup
    }

    ProductGroup findOrCreateProductGroup(String name) {
        ProductGroup productGroup = ProductGroup.findByName(name)
        if (!productGroup) {
            productGroup = new ProductGroup(name: name)
            productGroup.save(failOnError: true)
        }
        return productGroup
    }
}
