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

class AutoSuggestStringTagLib {


    //@Cacheable("autoSuggestStringTagCache")
    def autoSuggestString = { attrs, body ->
        out << g.render(template: '/taglib/autoSuggestString', model: [attrs: attrs])
    }

    def autoSuggestString_v1 = { attrs, body ->
        def id = (attrs.id) ? attrs.id : "autoSuggest_" + (new Random()).nextInt()
        def name = attrs.name
        def value = (attrs.value) ? attrs.value : ""
        def width = (attrs.width) ? attrs.width : 200
        def minLength = (attrs.minLength) ? attrs.minLength : 1
        def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : ""
        def styleClass = (attrs.styleClass) ?: ""
        // def showValue = (value) ? true : false;
        // def spanDisplay = (showValue) ? "inline" : "none";
        // def suggestDisplay = (showValue) ? "none" : "inline";
        // def spanDisplay = "none";
        def suggestDisplay = "inline"

        def html = """
			<span>
				<style>
					#${id}-suggest {
						background-image: url('${request.contextPath}/images/icons/silk/magnifier.png');
						background-repeat: no-repeat;
						background-position: center left;
						padding-left: 20px;
					}
				</style>
				
				<input id="${id}" type="hidden" name="${name}" value="${value}"/>
				<input id="${id}-suggest" type="text" class="${styleClass}" name="${
            name
        }.autoSuggest" value="${value}" style="width: ${width}px; display: ${suggestDisplay};">
				
				
				<script>
					\$(document).ready(function() {
						
						\$("#${id}-suggest").autocomplete({
							width: ${width},
							minLength: ${minLength},
							dataType: 'json',
							highlight: true,
							//selectFirst: true,
							scroll: true,
							autoFill: true,
							//scrollHeight: 300,
							//define callback to format results
							source: function(req, add){
							
								var url = '${jsonUrl}';
								\$.getJSON(url, req, function(data) {
									var items = [];
									\$.each(data, function(i, item) {
										items.push(item);
									});
									add(items);
								});
							  },
							focus: function(event, ui) {
								console.log(event);
								\$(this).trigger("focus");
							},
							change: function(event, ui) {
								\$('#${id}').val(\$(this).val());
								//\$('#${id}-suggest').val(ui.item.valueText);
								console.log(\$(this));
								console.log(event);
								\$(this).trigger("change");
							},
							select: function(event, ui) {
								//alert("selected " + ui.item.value + " " + ui.item.valueText);
								\$('#${id}').val(ui.item.value);
								\$('#${id}-suggest').val(ui.item.valueText);
								\$('#${id}-suggest').trigger('selected');
								return false;
							}
						});
					});
					
				</script>
			</div>
		"""


        out << html
    }
}