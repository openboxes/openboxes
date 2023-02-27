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

import org.pih.warehouse.product.Category
import org.pih.warehouse.product.ProductCatalog

class SelectOptionsService {

    def getGlAccountsOptions() {
        return GlAccount.list().collect {
            [id: it.id, label: "${it.code} - ${it.name}"]
        }
    }

    def getTagsOptions() {
        return Tag.list(sort: "tag").collect {
            [id: it.id, label: "${it.tag} (${it?.products?.size()})"]
        }
    }

    def getCatalogsOptions() {
        return ProductCatalog.list(sort: "name").collect {
            [id: it.id, label: "${it.name} (${it?.productCatalogItems?.size()})"]
        }
    }

    def getCategoryOptions() {
        return Category.list().sort().collect {
            [id: it.id, label: it.getHierarchyAsString(" > ")]
        }
    }
}
