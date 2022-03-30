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

class ProductCatalogService {

    ProductCatalogItem createOrUpdateProductCatalogItem(Map params) {
        Product product
        ProductCatalog productCatalog

        if (params.productCode) {
            product = Product.findByProductCode(params.productCode)
        }

        if (params.productCatalogCode) {
            productCatalog = ProductCatalog.findByCode(params.productCatalogCode)
        }

        ProductCatalogItem productCatalogItem = ProductCatalogItem.findByProductAndProductCatalog(product, productCatalog)
        if (!productCatalogItem) {
            productCatalogItem = new ProductCatalogItem()
            productCatalogItem.product = product
            productCatalogItem.productCatalog = productCatalog
        }

        return productCatalogItem
    }
}
