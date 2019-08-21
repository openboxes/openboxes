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


class AutoSuggestSearchableTagLib {

    def autoSuggestSearchable = { attrs, body ->
        attrs.id = (attrs.id) ? attrs.id : "autoSuggest_" + (new Random()).nextInt()
        attrs.name = attrs.name
        attrs.styleClass = attrs.styleClass
        attrs.valueId = (attrs.valueId) ?: ""
        attrs.valueName = (attrs.valueName) ?: ""
        attrs.width = (attrs.width) ?: '200px'
        attrs.minLength = (attrs.minLength) ?: 1
        attrs.jsonUrl = (attrs.jsonUrl) ?: ""

        attrs.suggestDisplay = "inline"
        out << g.render(template: '/taglib/autoSuggestSearchable', model: [attrs: attrs])

    }

}