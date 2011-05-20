package org.pih.warehouse

import java.text.SimpleDateFormat;

class AutoSuggestSearchableTagLib {
		

	def autoSuggestSearchable = { attrs, body ->
		def id = (attrs.id) ? attrs.id : "autoSuggest_" + (new Random()).nextInt()
		def name = attrs.name
		def valueId = (attrs.valueId)?:"";
		def valueName = (attrs.valueName)?:"";
		def width = (attrs.width) ?: 200;
		def minLength = (attrs.minLength) ?: 1;
		def jsonUrl = (attrs.jsonUrl) ?: "/warehouse/json/findPersonByName";

		def showValue = (valueName && valueId) ? true : false;
		//def spanDisplay = (showValue) ? "inline" : "none";
		//def suggestDisplay = (showValue) ? "none" : "inline";
		def spanDisplay = "none";
		def suggestDisplay = "inline";
		
		def html = """
			<div>
				<style>
					#${id}-suggest {
						background-image: url('/warehouse/images/icons/silk/magnifier.png');
						background-repeat: no-repeat;
						background-position: center left;
						padding-left: 20px;						
					}				
					.ui-autocomplete-term { font-weight: bold; color: #DDD; }
				</style>
				
				<input id="${id}-suggest" type="text" name="${name}.name" 
					value="${valueName}" style="width: ${width}px; display: ${suggestDisplay};"> 	
				
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
					        },	
					        change: function(event, ui) { 
					        },
							select: function(event, ui) {
								// text display
								\$("#lotNumber-text").html(ui.item.lotNumber);
								\$("#product-text").html(ui.item.productName);
								\$("#quantity-text").html(ui.item.quantity);
								
								// product hidden values
								\$("#product-id").val(ui.item.productId);
								\$("#lotNumber").val(ui.item.lotNumber);
								\$("#quantity").val(ui.item.quantity);
																
								//
								\$("#itemFoundForm").show();
								\$("#itemSearchForm").hide();
								
							}
						});
					});
					
				</script>
			</div>		
		""";
			
		
		out << html; 
	}
	
}