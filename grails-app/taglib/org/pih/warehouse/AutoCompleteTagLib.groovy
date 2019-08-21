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

// import java.text.SimpleDateFormat

class AutoCompleteTagLib {

    def autoComplete = { attrs, body ->
        def id = attrs.id
        def name = attrs.name
        def valueId = (attrs.valueId) ? attrs.valueId : ""
        def valueName = (attrs.valueName) ? attrs.valueName : ""
        def width = (attrs.width) ? attrs.width : 200
        // def minLength = (attrs.minLength) ? attrs.minLength : 1;
        def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : ""

        // def showValue = (valueName && valueId) ? true : false;
        //def spanDisplay = (showValue) ? "inline" : "none";
        //def suggestDisplay = (showValue) ? "none" : "inline";
        def spanDisplay = "none"
        def suggestDisplay = "inline"

        def html = """
			<div>
				<style>
					#${id}-suggest {
						background-image: url('${request.contextPath}/images/icons/silk/magnifier.png');
						background-repeat: no-repeat;
						background-position: center left;
						padding-left: 20px;
					}
				</style>
				
				<input id="${id}-id" type="hidden" name="${name}.id" value="${valueId}"/>
				<input id="${id}-suggest" type="text" name="${name}.name" value="${
            valueName
        }" style="width: ${width}px; display: ${suggestDisplay};">
				<span id="${id}-span" style="text-align: left; display: ${spanDisplay};">${
            valueName
        }</span>
				
				<script>
					\$(document).ready(function() {
							\$("#${id}-suggest").autocomplete( {
								source: function(req, add){
									\$.getJSON('${jsonUrl}', req, function(data) {
										var items = [];
										\$.each(data, function(i, item) {
											items.push(item);
										});
										add(items);
									});
								  }
							
							});
					});
					
				</script>
			</div>
		"""


        out << html
    }

}