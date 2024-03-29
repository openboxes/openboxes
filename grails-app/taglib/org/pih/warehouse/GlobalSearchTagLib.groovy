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


class GlobalSearchTagLib {

    def globalSearch = { attrs, body ->

        attrs.name = (attrs.name) ? attrs.name : attrs.id
        attrs.value = (attrs.value) ?: ""
        attrs.width = (attrs.width) ?: ""
        attrs.minLength = (attrs.minLength) ?: 1
        attrs.jsonUrl = (attrs.jsonUrl) ?: ""
        attrs.cssClass = (attrs.cssClass) ?: ""
        attrs.size = (attrs.size) ?: "150"
        attrs.display = (attrs.display) ?: "visible"

        if (attrs.buttonId) {
            out << g.render(template: '/taglib/globalSearch', model: [attrs: attrs])
        } else {
            out << g.render(template: '/taglib/globalSearchStatic', model: [attrs: attrs])
        }
    }

}
