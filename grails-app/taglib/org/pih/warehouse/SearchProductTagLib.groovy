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

class SearchProductTagLib {

    def searchProduct = { attrs, body ->

        /*
		def id = (attrs.id) ? attrs.id : "autoSuggest_" + (new Random()).nextInt()
		def name = attrs.name
		def valueId = (attrs.valueId)?attrs.valueId:"";
		def valueName = (attrs.valueName)?attrs.valueName:"";
		def width = (attrs.width) ? attrs.width : '300px';
		def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : "";
		def styleClass = attrs.styleClass ?: ''
		def placeholder = attrs.placeholder ?: ""
        def minLength = (attrs.minLength) ?: 1

		def spanDisplay = "none";
		def suggestDisplay = "inline";
        */

        out << render(template: "../taglib/searchProduct", model: [attrs: attrs])
    }

}