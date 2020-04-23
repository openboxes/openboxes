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


class AutoSuggestTagLib {

    def chooseSubstitute = { attrs, body ->
        attrs.id = (attrs.id) ? attrs.id : "chooseSubstitute_" + (new Random()).nextInt()
        attrs.name = attrs.name
        attrs.valueId = (attrs.valueId) ? attrs.valueId : ""
        attrs.valueName = (attrs.valueName) ? attrs.valueName : ""
        attrs.width = (attrs.width) ? attrs.width : '300px'
        attrs.minLength = (attrs.minLength) ? attrs.minLength : 1
        attrs.jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : ""
        attrs.styleClass = (attrs.styleClass) ?: ""
        attrs.placeholder = attrs.placeholder ?: ""


        out << g.render(template: '/taglib/chooseSubstitute', model: [attrs: attrs])
    }


    def autoSuggest = { attrs, body ->
        attrs.id = (attrs.id) ? attrs.id : "autoSuggest_" + (new Random()).nextInt()
        attrs.name = attrs.name
        attrs.valueId = (attrs.valueId) ? attrs.valueId : ""
        attrs.valueName = (attrs.valueName) ? attrs.valueName : ""
        attrs.width = (attrs.width) ? attrs.width : '100%'
        attrs.size = (attrs.size) ? attrs.size : 30
        attrs.delay = (attrs.delay) ? attrs.delay : grailsApplication.config.openboxes.typeahead.delay
        attrs.minLength = (attrs.minLength) ? attrs.minLength : grailsApplication.config.openboxes.typeahead.minLength
        attrs.jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : ""
        attrs.styleClass = attrs.styleClass ?: ''
        attrs.placeholder = attrs.placeholder ?: ""
        attrs.spanDisplay = "none"
        attrs.suggestDisplay = "inline"
        attrs.valueDataBind = attrs.valueDataBind ? "data-bind='${attrs.valueDataBind}'" : ""
        attrs.textDataBind = attrs.valueDataBind ? "data-bind='${attrs.textDataBind}'" : ""

        out << g.render(template: '/taglib/autoSuggest', model: [attrs: attrs])
    }

}
