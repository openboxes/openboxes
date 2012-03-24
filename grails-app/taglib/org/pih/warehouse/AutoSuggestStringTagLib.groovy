package org.pih.warehouse

import java.text.SimpleDateFormat;
import org.pih.warehouse.core.Person;

class AutoSuggestStringTagLib {
		

	def autoSuggestString = { attrs, body ->
		def id = (attrs.id) ? attrs.id : "autoSuggest_" + (new Random()).nextInt()
		def name = attrs.name
		def value = (attrs.value)?attrs.value:"";
		def width = (attrs.width) ? attrs.width : 200;
		def minLength = (attrs.minLength) ? attrs.minLength : 1;
		def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : "";
		def styleClass = (attrs.styleClass)?:"";
		def showValue = (value) ? true : false;
		//def spanDisplay = (showValue) ? "inline" : "none";
		//def suggestDisplay = (showValue) ? "none" : "inline";
		def spanDisplay = "none";
		def suggestDisplay = "inline";
		
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
				
				<input id="${id}" type="hidden" name="${name}" value="${value}"/>
				<input id="${id}-suggest" type="text" class="${styleClass}" name="${name}.autoSuggest" value="${value}" style="width: ${width}px; display: ${suggestDisplay};">
				
				
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
								return false;
							}
						});
					});
					
				</script>
			</div>
		""";
			
		
		out << html;
	}
}