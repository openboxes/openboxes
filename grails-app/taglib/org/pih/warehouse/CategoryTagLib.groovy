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

class CategoryTagLib {

    static Integer counter = 0

    def displayTree = { categories, beginTag, endTag ->
        counter++
        categories.each {
            log.info beginTag + it + endTag
            log.info counter.toString()
            log.info "display children: " + it.categories
            displayTree it.categories, "<h3>", "</h3>"
        }
    }

    def selectCategoryWithChosen = { attrs ->
        out << """
			<select multiple="true" data-placeholder="${attrs.noSelection.value}" name="${
            attrs.name
        }" style="${attrs.style}" value="${attrs.value}" class='${attrs.class}'>
		"""
        displayCategoryOptions(attrs['rootNode'], attrs.value, 0)
        out << "</select>"
    }

    def displayCategoryOptions = { node, value, depth ->
        if (node) {
            if (node.id) {
                println value?.id + " == " + node?.id
                def selected = (value == node)
                out << """<option value="${node?.id}" ${selected ? "selected" : ""}>${
                    includeIndent(depth) + node?.name
                }</option>"""
            }
            if (node.categories) {
                //out << includeIndent(depth)
                node.categories.each {
                    displayCategoryOptions(it, value, depth + 1)
                }
            }
        }
    }

    def includeIndent = { howMany ->
        def indent = ""
        while (howMany-- > 0) {
            indent += "-"
        }
        indent
    }
}
