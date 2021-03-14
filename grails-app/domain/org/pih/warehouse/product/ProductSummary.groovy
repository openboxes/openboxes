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

import org.apache.commons.lang.builder.HashCodeBuilder
import org.pih.warehouse.core.Location

class ProductSummary implements Serializable {

    Product product
    Location location
    BigDecimal quantityOnHand
    BigDecimal quantityOnOrder
    BigDecimal quantityOnOrderNotShipped
    BigDecimal quantityOnOrderNotReceived

    static mapping = {
        id composite: ["product", "location"]
        version false
        cache usage: "read-only"
    }

    static constraints = {
        product(nullable: false)
        location(nullable: false)
        quantityOnHand(nullable: false)
        quantityOnOrder(nullable: true)
        quantityOnOrderNotShipped(nullable: true)
        quantityOnOrderNotReceived(nullable: true)
    }

    boolean equals(other) {
        if (!(other instanceof ProductSummary)) {
            return false
        }
        other.product?.id == product?.id && other.location?.id == location?.id
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        builder.append(product?.id)
        builder.append(location?.id)
        builder.toHashCode()
    }
}
