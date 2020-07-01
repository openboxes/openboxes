/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.core

import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage

class UomService {

    boolean transactional = false

    ProductPackage getProductPackage(Product product, UnitOfMeasure unitOfMeasure, Integer quantity) {
        return ProductPackage.createCriteria().get {
            eq("product", product)
            eq("uom", unitOfMeasure)
            eq("quantity", quantity)
            isNull("productSupplier")
        }
    }
}
