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

import java.text.SimpleDateFormat;

class AutoSuggestSearchableTagLib {
		

	def autoSuggestSearchable = { attrs, body ->
		def id = (attrs.id) ? attrs.id : "autoSuggest_" + (new Random()).nextInt()
		def name = attrs.name
		def styleClass = attrs.styleClass
		def valueId = (attrs.valueId)?:"";
		def valueName = (attrs.valueName)?:"";
		def width = (attrs.width) ?: 200;
		def minLength = (attrs.minLength) ?: 1;
		def jsonUrl = (attrs.jsonUrl) ?: "";

		def showValue = (valueName && valueId) ? true : false;
		//def spanDisplay = (showValue) ? "inline" : "none";
		//def suggestDisplay = (showValue) ? "none" : "inline";
		def spanDisplay = "none";
		def suggestDisplay = "inline";
		
		def html = """
			<span>
				<style>
					#${id}-suggest {
						background-image: url('${request.contextPath}/images/icons/silk/magnifier.png');
						background-repeat: no-repeat;
						background-position: center left;
						padding-left: 20px;						
					}				
					.ui-autocomplete-term { font-weight: bold; color: #DDD; }
				</style>
				
				<input id="${id}-suggest" type="text" name="${name}.name" 
					value="${valueName}" style="width: ${width}px; display: ${suggestDisplay};" class="${styleClass}"> 	
				
				<script>
					\$(document).ready(function() {
				      	\$("#${id}-suggest").autocomplete({
				            width: ${width},
				            minLength: ${minLength},
				            dataType: 'json',
				            highlight: true,
				            //selectFirst: true,
				            scroll: true,
				            autoFocus: true,
				            autoFill: true,
				            //scrollHeight: 300,
							//define callback to format results
							source: function(request, response){			
								\$.getJSON('${jsonUrl}', request, function(data) {
									var suggestions = [];
									\$.each(data, function(i, item) {
										suggestions.push(item);
									});
									response(suggestions);
								});
					      	},
					        focus: function(event, ui) {			
					        	return false;
					        },	
					        change: function(event, ui) { 
					        	return false;
					        },
							select: function(event, ui) {
								// set text display
								\$("#lotNumber-text").html(ui.item.lotNumber);
								\$("#product-text").html(ui.item.productName);
								\$("#quantity-text").html(ui.item.quantity);
								\$("#expirationDate-text").html(ui.item.expirationDate);

								// set hidden values
								\$("#productId").val(ui.item.productId);
								\$("#lotNumber-suggest").val(ui.item.lotNumber);
								\$("#inventoryItemId").val(ui.item.id)
								//\$("#quantity").val(ui.item.quantity);

								// Update on hand quantity
								updateQuantityOnHand();			

								\$("#itemFoundForm").show();
								\$("#itemSearchForm").hide();
								\$("#quantity").focus();
								
							}
						});
					});
					
				</script>
			</span>		
		""";
			
		
		out << html; 
	}
	
}