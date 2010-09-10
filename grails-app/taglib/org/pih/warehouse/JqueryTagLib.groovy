package org.pih.warehouse

import java.text.SimpleDateFormat;
import org.pih.warehouse.core.Person;

class JqueryTagLib {
		
	def autoSuggest = { attrs, body ->
		def id = attrs.id
		def name = attrs.name	
		def valueId = (attrs.valueId)?attrs.valueId:"";
		def valueName = (attrs.valueName)?attrs.valueName:"";
		def width = (attrs.width) ? attrs.width : 200;
		def minLength = (attrs.minLength) ? attrs.minLength : 2;
		def jsonUrl = (attrs.jsonUrl) ? attrs.jsonUrl : "findPersonByName";
		
		def html = """
			<div>
				<style>
					#${id}-suggest {
						background-image: url('/warehouse/images/icons/silk/magnifier.png');
						background-repeat: no-repeat;
						background-position: center right;
						padding-right: 20px;						
					}				
				</style>
			
				<input id="${id}-id" type="hidden" name="${name}.id" value="${valueId}"/>
				<input id="${id}-suggest" type="text" name="${name}.name" value="${valueName}" style="width: ${width}px;"> 	
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
								\$('#${id}-id').val(ui.item.value);
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
	
	
	def jqueryDatePicker = {attrs, body ->
		
		def id = attrs.id
		def name = attrs.name;
		
		def value = (attrs.format && attrs.value) ? new SimpleDateFormat(attrs.format).format(attrs.value) : ""

		if (name == null) { 
			throw new IllegalArgumentException("name parameter must be specified")			
		}
		def html = """

		<div>
			<style>
				.ui-datepicker-trigger { 
					position: relative; left: -20px; top: 2px; 
				}
			</style>
			<input id='${id}' name='${name}' type='hidden'/> 
			<input id='${id}-datepicker' name='${name}-datepicker' type='text' class='date' /> 
			<script type=\'text/javascript\'> 
				jQuery(function() {
					var dateValue = new Date('${value}'); 	
					jQuery('#${id}-datepicker').datepicker({
						showOn: 'both',
						altField: '#${name}',
						altFormat: 'mm/dd/yy',
						dateFormat: 'MM dd yy',
						//autoSize: true,
						//closeText: 'Done',
						buttonImageOnly: true, 
						buttonImage: '/warehouse/images/icons/silk/calendar.png',
						//buttonText: '...',
						//showButtonPanel: true,
						//showOtherMonths: true,
						//selectOtherMonths: true
					});
					jQuery('#${name}-datepicker').datepicker('setDate', dateValue);
				}); 
			</script> 
		</div>
		""";
		
		out << html;
				
	}
}
