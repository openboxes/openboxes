package org.pih.warehouse

import java.text.SimpleDateFormat;
import org.pih.warehouse.core.Person;

class JqueryTagLib {
	
	
	
	def personAutoSuggest = { attrs, body ->
		def name = attrs.name	
		def person = Person.get(attrs.id);
		
		def personAutoSuggestHtml = """
			<div>
				<input id="carrier-suggest" type="text" value=""/> 	
				<img id="carrier-icon" src="/warehouse/images/icons/search.png" style="vertical-align: middle;"/>
				<input id="carrier-id" name="carrier.id" type="hidden" value=""/>
				<span id="carrier-name"></span>		
			</div>		
			<script>
				\$(document).ready(function() {
					\$('#carrier-suggest').focus();
					\$("#carrier-name").click(function() {
						\$('#carrier-suggest').val("");
						\$('#carrier-name').hide();
						\$('#carrier-suggest').show();				
						\$('#carrier-suggest').focus();
						\$('#carrier-suggest').select();
					});
			      	\$("#carrier-suggest").autocomplete({
			            width: 400,
			            minLength: 2,
			            dataType: 'json',
			            highlight: true,
			            selectFirst: true,
			            scroll: true,
			            autoFill: true,
			            //scrollHeight: 300,
						//define callback to format results
						source: function(req, add){
							//pass request to server
							\$.getJSON("/warehouse/test/searchByName", req, function(data) {
								var people = [];
								\$.each(data, function(i, item){
									people.push(item);
								});
								add(people);
							});
				      	},
				        focus: function(event, ui) {			        
				      		\$('#carrier-suggest').val(ui.item.label);					
				      		return false;
				        },	
						select: function(event, ui) {	
							\$('#carrier-suggest').val(ui.item.label);
							\$('#carrier-name').html(ui.item.label);
							\$('#carrier-id').val(ui.item.value);
							\$('#carrier-icon').attr('src', '/warehouse/images/icons/silk/user.png');
							\$('#carrier-suggest').hide();
							\$('#carrier-name').show();
							return false;
						}
					});
				});
			</script>""";
			
		
		out << personAutoSuggestHtml; 
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
