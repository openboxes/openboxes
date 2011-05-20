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
			<div>
				<style>
					#${id}-suggest {
						background-image: url('/warehouse/images/icons/silk/magnifier.png');
						background-repeat: no-repeat;
						background-position: center left;
						padding-left: 20px;
					}
				</style>
				
				<input id="${id}-id" type="hidden" name="${name}.id" value="${valueId}"/>
				<span id="${id}-span" style="text-align: left; display: ${spanDisplay};">${valueName}</span>
				<input id="${id}-suggest" type="text" name="${name}.name" value="${valueName}" style="width: ${width}px; display: ${suggestDisplay};">
				
				
				<script>
					\$(document).ready(function() {
						// Captures 'Enter' key presses
						//\$(window).keydown(function(event){
						//	if(event.keyCode == 13) {
						//		event.preventDefault();
						//		return false;
						//	}
						//});
						
						\$("#${id}-suggest").click(function() {
							\$("#${id}-suggest").trigger("focus");
						});
						
						\$("#${id}-suggest").blur(function() {
							var text = \$('#${id}-suggest').val();
							//\$('#${id}-suggest').hide();
							//\$('#${id}-span').html(text?text:'<b>empty</b> &nbsp; click to change');
							//\$('#${id}-span').show();
						});
						\$("#${id}-span").click(function() {
							//\$('#${id}-span').hide();
							//\$('#${id}-suggest').show();
							//\$('#${id}-suggest').val('');
							//\$('#${id}-span').html('');
							//\$('#${id}-id').val('');
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
								  //\$('#${id}-suggest').val(ui.item.valueText);
								  return false;
							},
							change: function(event, ui) {
								//alert("changed " + \$(this).val());
								//\$('#${id}-id').val('');
								//\$('#${id}-suggest').val('');
								return false;
							},
							select: function(event, ui) {
								\$('#${id}-id').val(ui.item.value);
								\$('#${id}-suggest').val(ui.item.valueText);
								\$('#${id}-span').html(ui.item.valueText);
								//\$('#${id}-suggest').hide();
								//\$('#${id}-span').show();
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