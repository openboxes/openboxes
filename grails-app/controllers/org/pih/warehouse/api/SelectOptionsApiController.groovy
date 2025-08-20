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
import org.pih.warehouse.core.PaymentTerm
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.core.RatingTypeCode
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.data.ProductSupplierService
import org.pih.warehouse.glAccount.GlAccountService
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductField
import org.pih.warehouse.product.ProductGroup

class SelectOptionsApiController {

    GenericApiService genericApiService;
    GlAccountService glAccountService;
    UserService userService

    def glAccountOptions() {
        List<GlAccount> glAccounts = glAccountService.getGlAccounts(params)
                .findAll { it?.code && it?.name }
                .collect {
                    [id: it.id, label: "${it.code} - ${it.name}"]
                }
        render([data: glAccounts] as JSON)
    }

    def productGroupOptions() {
        List<ProductGroup> productGroups = genericApiService.getList(ProductGroup.class.simpleName, [:])
                .findAll { it?.name }
                .collect {
                    [id: it.id, label: "${it.name}"]
                }
        render([data: productGroups] as JSON)
    }

    def catalogOptions() {
        boolean hideNumbers = params.boolean("hideNumbers", false)

        def catalogs = genericApiService.getList(ProductCatalog.class.simpleName, [sort: "name"])
                .findAll { it?.name }
                .collect {
                    [id: it.id, label: hideNumbers ? it.name : "${it.name} (${it?.productCatalogItems?.size()})"]
                }
        render([data: catalogs] as JSON)
    }

    def categoryOptions() {
        List<Category> categories = genericApiService.getList(Category.class.simpleName, [:])
                .findAll { it.getHierarchyAsString(" > ") }
                .collect {
                    [id: it.id, label: it.getHierarchyAsString(" > ")]
                }
        render([data: categories] as JSON)
    }

    def tagOptions() {
        boolean hideNumbers = params.boolean("hideNumbers", false)

        def tags = genericApiService.getList(Tag.class.simpleName, [sort: "tag"])
                .findAll { it?.tag }
                .collect {
                    [id: it.id, label: hideNumbers ? it.tag : "${it.tag} (${it?.products?.size()})"]
                }
        render([data: tags] as JSON)
    }

    def paymentTermOptions() {
        List<PaymentTerm> paymentTerms = genericApiService.getList(PaymentTerm.class.simpleName, [sort: "name"])
                .findAll { it?.name }
                .collect {
                    [id: it.id, label: it.name, value: it.id ]
                }
        render([data: paymentTerms] as JSON)
    }

    def usersOptions() {
        params.roleTypes = params.list("roleTypes")
        if (params.roleTypes && !params.location) {
            params.location = session.warehouse?.id
        }
        List<User> users = userService.findUsers(params)
        render([data: users] as JSON)
    }

    def preferenceTypeOptions() {
        List<Map<String, String>> preferenceTypeOptions = []

        boolean includeMultiple = params.boolean("includeMultiple", false)
        if (includeMultiple) {
            preferenceTypeOptions.add([
                id: ProductSupplierService.PREFERENCE_TYPE_MULTIPLE,
                value: ProductSupplierService.PREFERENCE_TYPE_MULTIPLE,
                label: g.message(code: "react.productSupplier.preferenceType.multiple.label", default: "Multiple")
            ])
        }

        boolean includeNone = params.boolean("includeNone", false)
        if (includeNone) {
            preferenceTypeOptions.add([
                id: ProductSupplierService.PREFERENCE_TYPE_NONE,
                value: ProductSupplierService.PREFERENCE_TYPE_NONE,
                label: g.message(code: 'react.productSupplier.preferenceType.none.label', default: "None")
            ])
        }

        List<Map<String, String>> preferenceTypes = genericApiService.getList(PreferenceType.class.simpleName, [:])
                .findAll { it?.name }
                .collect {
                    [id: it.id, label: it.name, value: it.id ]
                }

        preferenceTypeOptions.addAll(preferenceTypes)

        render([data: preferenceTypeOptions] as JSON)
    }

    def ratingTypeCodeOptions() {
        List ratingTypeCodeOptions = RatingTypeCode.list().collect {
            [id: it.name, value: it.name, label: g.message(code: "enum.RatingTypeCode.$it.name", default: it.name)]
        }
        render([data: ratingTypeCodeOptions] as JSON)
    }

    def handlingRequirementsOptions() {
        List<ProductField> handlingRequirements = [
                ProductField.COLD_CHAIN,
                ProductField.CONTROLLED_SUBSTANCE,
                ProductField.HAZARDOUS_MATERIAL,
                ProductField.RECONDITIONED
        ].collect {
            [id: it.name(), value: it.name(), label: g.message(code: "enum.ProductField.${it.name()}", default: it.name())]
        }
        render([data: handlingRequirements] as JSON)
    }
}
