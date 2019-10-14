/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse

import org.apache.commons.lang.StringUtils
import org.pih.warehouse.product.Category

class CategorySelectTagLib {

    def productService
    def inventoryService

    def getCategories(node) {
        def array = []
        array << node
        if (node.categories) {
            for (Category c : node.categories) {
                def categories = getCategories(c)
                categories.each {
                    array << it
                }
            }
        } else {
            return node
        }

        return array
    }


    def selectCategory_v2 = { attrs ->
        attrs.from = getCategories(productService.getRootCategory())
        attrs.optionKey = 'id'
        attrs.noSelection = ['null': '-Choose a category-']
        attrs.value = attrs.value

        if (attrs.abbreviate) {
            attrs.optionValue = {
                StringUtils.abbreviate(it.name, 50) + " (" + it?.products?.size() + ")"
            }
        } else {
            attrs.optionValue = { it.name + " (" + it?.products?.size() + ")" }
        }
        out << g.select(attrs)
    }


    def categorySelect = { attrs ->
        def selectedCategory = Category.get(attrs.value)
        def rootCategory = productService.getRootCategory()
        def excludeSpaces = attrs?.excludeSpaces

        out << "<select class='" + attrs.cssClass + "' id='" + attrs.id + "' name='" + attrs.name + "'>"
        out << render(template: "../category/selectOptions", model: [category: rootCategory, selected: selectedCategory, level: 0, excludeSpaces: excludeSpaces])
        out << "</select>"
    }
}
