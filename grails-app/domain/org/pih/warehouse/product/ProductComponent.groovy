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

import org.pih.warehouse.core.UnitOfMeasure

class ProductComponent {

    String id
    Product componentProduct
    BigDecimal quantity
    UnitOfMeasure unitOfMeasure

    Date dateCreated
    Date lastUpdated


    static belongsTo = [assemblyProduct: Product]

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        componentProduct(nullable: false)
        quantity(nullable: false)
        unitOfMeasure(nullable: false)
        assemblyProduct(nullable: false)
    }
}
