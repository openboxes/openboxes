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

class MenuTagLib {

    static result = ""

    def displayMenuHtml = { node, depth ->
        if (node) {
            if (node.id) {
                out << includeIndent(depth) + "<li><a href=\"browse?browseBy=category&categoryId=${node?.id}\">" + node.name + "</a></li>"
            }
            if (node.categories) {
                out << includeIndent(depth) + "<ul>"
                node.categories.each { displayMenuHtml(it, depth + 1) }
                out << includeIndent(depth) + "</ul>"
            }
        }
    }

    /**
     * Used to make the HTML a little easier to read.
     */
    def includeIndent = { howMany ->
        def indent = ""
        while (howMany-- > 0) {
            indent += "\t"
        }
        indent
    }


}
