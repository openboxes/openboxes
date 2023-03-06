/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.Tag
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductGroup

class SelectOptionsApiController {

    GenericApiService genericApiService;

    def glAccountOptions = {
        def glAccounts = genericApiService.getList(GlAccount.class.simpleName, [:]).collect {
            [id: it.id, label: "${it.code} - ${it.name}"]
        }
        render([data: glAccounts] as JSON)
    }

    def productGroupOptions = {
        def productGroups = genericApiService.getList(ProductGroup.class.simpleName, [:]).collect {
            [id: it.id, label: "${it.name}"]
        }
        render([data: productGroups] as JSON)
    }

    def catalogOptions = {
        def catalogs = genericApiService.getList(ProductCatalog.class.simpleName, [sort: "name"]).collect {
            [id: it.id, label: "${it.name} (${it?.productCatalogItems?.size()})"]
        }
        render([data: catalogs] as JSON)
    }

    def categoryOptions = {
        def categories = genericApiService.getList(Category.class.simpleName, [:]).collect {
            [id: it.id, label: it.getHierarchyAsString(" > ")]
        }
        render([data: categories] as JSON)
    }

    def tagOptions = {
        def tags = genericApiService.getList(Tag.class.simpleName, [sort: "tag"]).collect {
            [id: it.id, label: "${it.tag} (${it?.products?.size()})"]
        }
        render([data: tags] as JSON)
    }

}
