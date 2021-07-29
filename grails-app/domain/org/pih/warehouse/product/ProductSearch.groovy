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

import org.pih.warehouse.core.Location

class ProductSearch {

    String id

    Product product
    Location location
    ProductType type
    Boolean isSearchableType = Boolean.FALSE

    // Quantities
    Integer quantityOnHand
    Integer quantityAllocated
    Integer quantityOnHold
    Integer quantityAvailableToPromise


    static mapping = {
        id generator: 'uuid'
        version false
        cache usage: "read-only"
    }

    static constraints = {}

    static transients = ['color']

    String getColor() {
        return product?.color ?: ''
    }
}
