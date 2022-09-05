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

import org.codehaus.groovy.grails.commons.ConfigurationHolder

class ProductTypeService {

    synchronized getAndSetNextSequenceNumber(ProductType productType) {
        productType.sequenceNumber = productType.getNextSequenceNumber()
        productType.save()

        return productType.sequenceNumber
    }

    def getDefaultProductType() {
        def defaultProductTypeId = ConfigurationHolder.config.openboxes.identifier.defaultProductType.id
        if (!defaultProductTypeId) {
            throw new IllegalArgumentException("Missing default product type configuration")
        }

        def defaultProductType = ProductType.get(defaultProductTypeId)
        if (!defaultProductType) {
            throw new Exception("Can not find product type with id: ${defaultProductTypeId}")
        }

        return defaultProductType
    }
}
