package org.pih.warehouse

import java.text.SimpleDateFormat;
import org.pih.warehouse.core.Person;

class AutoSuggestTagLib {
		

	def autoSuggest = { attrs, body ->
		def id = (attrs.id) ? attrs.id : "autoSuggest_" + (new Random()).nextInt()
		def name = attrs.name
		def valueId = (attrs.valueId)?attrs.valueId:"";
		def valueName = (attrs.valueName)?attrs.valueName:"";
		def width = (attrs.width) ? attrs.width : 200;
		def minLength = (attrs.minLength) ? attrs.minLength : 1;
		def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : "/warehouse/json/findPersonByName";

		def showValue = (valueName && valueId) ? true : false;
		//def spanDisplay = (showValue) ? "inline" : "none";
		//def suggestDisplay = (showValue) ? "none" : "inline";
		def spanDisplay = "none";
		def suggestDisplay = "inline";
		
		def html = """
				<span id="${id}-span" class="span" style="text-align: left; display: ${spanDisplay};">${valueName}</span>
				<input id="${id}-value" class="value" type="hidden" name="${name}.id" value="${valueId}"/>
				<input id="${id}-suggest" class="autocomplete" type="text" name="${name}.name" value="${valueName}" style="width: ${width}px; display: ${suggestDisplay};">
				
				<script language="javascript">
					\$(document).ready(function() {
						\$("#${id}-suggest").click(function() {
							\$(this).trigger("focus");
						});
						\$("#${id}-suggest").blur(function() {
							return false;
						});
						\$("#${id}-span").click(function() {
							return false;
						});
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
								\$.getJSON('${jsonUrl}', req, function(data) {
									var items = [];
									\$.each(data, function(i, item) {
										items.push(item);
									});
									add(items);
								});
							},
							focus: function(event, ui) {
								return false;
							},
							change: function(event, ui) {
								// If the user does not select a value, we remove the value
								if (!ui.item) { 
									\$(this).prev().val("");
									\$(this).val("");
								}
								return false;
							},
							select: function(event, ui) {
								if (ui.item) { 
									\$(this).prev().val(ui.item.value);
									\$(this).val(ui.item.valueText);
								}
								return false;
							}
						});
					});
				</script>
		""";
			
		
		out << html;
	}
	
}