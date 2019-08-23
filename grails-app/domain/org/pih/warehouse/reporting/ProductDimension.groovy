/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.reporting

class ProductDimension {

    Long id
    Boolean active = Boolean.TRUE
    String productId
    String productCode
    String productName
    String genericProduct
    String categoryName
    String abcClass
    BigDecimal unitPrice
    BigDecimal unitCost

    static mapping = {
        id generator: 'increment'
        cache true
    }

    static constraints = {
        active(nullable: false)
        productId(nullable: false)
        productCode(nullable: false, blank: false)
        productName(nullable: false, blank: false)
        genericProduct(nullable: true)
        categoryName(nullable: false, blank: false)
        abcClass(nullable: true)
        unitPrice(nullable: true)
        unitCost(nullable: true)
    }
}
