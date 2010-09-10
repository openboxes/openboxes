package org.pih.warehouse

import java.text.SimpleDateFormat;
import org.pih.warehouse.core.Person;

class JqueryTagLib {
		
	def autoSuggest = { attrs, body ->
		def id = attrs.id
		def name = attrs.name	
		def valueId = (attrs.valueId)?attrs.valueId:"";
		def valueName = (attrs.valueName)?attrs.valueName:"";
		def width = (attrs.width) ? attrs.width : 300;
		def minLength = (attrs.minLength) ? attrs.minLength : 2;
		def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : "findPersonByName";
		
		def html = """
			<div>
				<input id="${id}-id" type="hidden" name="${name}.id" value="${valueId}"/>
				<input id="${id}-suggest" type="text" name="${name}.name" value="${valueName}" style="width: ${width}px;"> 		
			</div>		
			<script>
				\$(document).ready(function() {
			      	\$("#${id}-suggest").autocomplete({
			            width: ${width},
			            minLength: ${minLength},
			            dataType: 'json',
			            highlight: true,
			            selectFirst: true,
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
				      		\$('#${id}-suggest').val(ui.item.valueText);					
				      		return false;
				        },	
						select: function(event, ui) {	
							//alert("selected: " + ui.item.value + " " + ui.item.valueText)
							search_option = ui.item;		
							\$('#${id}-id').val(ui.item.value);
							\$('#${id}-suggest').val(ui.item.valueText);
							return false;
						}
					});
				});
			</script>
		""";
			
		
		out << html; 
	}
	
	
	def jqueryDatePicker = {attrs, body ->
		
		def name = attrs.name;
		
		def value = (attrs.format && attrs.value) ? new SimpleDateFormat(attrs.format).format(attrs.value) : ""

		if (name == null) { 
			throw new IllegalArgumentException("name parameter must be specified")			
		}
		def html = """
		<input id='${name}' name='${name}' type='hidden'/> 
		<input id='${name}-datepicker' name='${name}-datepicker' type='text' class='date' width='8' /> 
		<script type=\'text/javascript\'> 
			jQuery(function() {
				var dateValue = new Date('${value}'); 	
				jQuery('#${name}-datepicker').datepicker({
					showOn: 'both',
					altField: '#${name}',
					altFormat: 'mm/dd/yy',
					dateFormat: 'dd M yy',
					autoSize: true,
					closeText: 'Done',
					showButtonPanel: true,
					showOtherMonths: true,
					selectOtherMonths: true
				});
				jQuery('#${name}-datepicker').datepicker('setDate', dateValue);
			}); 
		</script> 
		""";
		
		out << html;
				
	}
}
