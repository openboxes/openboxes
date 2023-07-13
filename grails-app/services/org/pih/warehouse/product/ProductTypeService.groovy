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

import grails.gorm.transactions.Transactional

@Transactional
class ProductTypeService {

    synchronized getAndSetNextSequenceNumber(ProductType productType) {
        productType.sequenceNumber = productType.getNextSequenceNumber()
        productType.save()

        return productType.sequenceNumber
    }

    ProductType saveProductType(ProductType productType) {
        ProductType persistedProductType = null
        if (!productType.hasErrors()) {
            persistedProductType = productType.save()
        }
        return persistedProductType
    }

    void delete(ProductType productType) {
        productType.delete()
    }
}
