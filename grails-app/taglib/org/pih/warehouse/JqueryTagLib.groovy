package org.pih.warehouse

import java.text.SimpleDateFormat;
import org.pih.warehouse.core.Person;

class JqueryTagLib {
		
	def autoSuggest = { attrs, body ->
		def name = attrs.name	
		def valueId = (attrs.valueId)?attrs.valueId:"";
		def valueName = (attrs.valueName)?attrs.valueName:"";
		def width = (attrs.width) ? attrs.width : 300;
		def minLength = (attrs.minLength) ? attrs.minLength : 2;
		def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : "findPersonByName";
		
		def html = """
			<div>
				<input id="${name}-id" type="hidden" name="${name}.id" value="${valueId}"/>
				<input id="${name}-suggest" type="text" name="${name}.name" value="${valueName}" style="width: ${width}px;"> 		
			</div>		
			<script>
				\$(document).ready(function() {
			      	\$("#${name}-suggest").autocomplete({
			            width: ${width*2},
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
				      		\$('#${name}-suggest').val(ui.item.valueText);					
				      		return false;
				        },	
						select: function(event, ui) {	
							search_option = ui.item;		
							\$('#${name}-suggest').val(ui.item.valueText);
							\$('#${name}-id').val(ui.item.value);
							//\$('#${name}-name').html(ui.item.valueText);					
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
		
		out << "<input id='" + name + "' name='" + name + "' type='hidden'/> \n"
		out << "<input id='" + name + "Widget' name='" + name + "Widget' type='text' class='date' width='8' /> \n"

		out << "<script type=\'text/javascript\'> \n"
		out << "jQuery(function() { \n"
		out << "	// expects MM/dd/yyyy \n"
		out << "	var dateValue = new Date('" + value + "'); "		
		out << "	jQuery('#" + name + "Widget').datepicker({ \n"
		out << "		showOn: 'both', \n"
		out << "		altField: '#" + name + "', \n"
		out << "		altFormat: 'mm/dd/yy', \n"
		out << "		dateFormat: 'dd M yy', \n"
		out << "		autoSize: true, \n"
		out << "		closeText: 'Done', \n"
		out << "		showButtonPanel: true, \n"
		out << "		showOtherMonths: true, \n"
		out << "		selectOtherMonths: true \n"
		out << "	}); \n"
		out << "	jQuery('#" + name + "Widget').datepicker('setDate', dateValue);\n"
		out << "}); \n"
		out << "</script> \n"		
	}
}
