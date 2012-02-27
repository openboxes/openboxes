package org.pih.warehouse

import java.text.SimpleDateFormat;
import org.pih.warehouse.core.Person;

class DatePickerTagLib {
		
	
	
	def datePicker = { attrs, body ->
		//attrs.from = Shipper.list().sort { it?.name?.toLowerCase() }
		//attrs.optionKey = 'id'
		//attrs.value = ""
		//attrs.optionValue = { it.name }
		def datePickerTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.FormTagLib')		
		out << datePickerTagLib.datePicker.call(attrs)
	}

	def jqueryDatePicker = {attrs, body ->
		
		def id = attrs.id ? attrs.id : attrs.name;
		def name = attrs.name;
		def autoSize = attrs.autoSize ?:(attrs.size)?"false":"true";
		def size = attrs.size ?: "10"
		def cssClass = attrs.cssClass ?: ""
		def showOn = attrs.showOn ?: "both";
		def showTrigger = Boolean.valueOf(attrs.showTrigger ?: "true");
		def changeMonthAndYear = attrs.changeMonthAndYear ?: "false";
		def showClear = attrs.showClear ?: "false"
		def value = attrs.value;
		if (value) { 
			if (value instanceof Date) {
				value = (attrs.format && attrs.value) ? new SimpleDateFormat(attrs.format).format(attrs.value) : ""
			} 
		}
			
		if (name == null) {
			throw new IllegalArgumentException("name parameter must be specified")
		}
		
		def html = """

		<span>
			<input id='${id}' name='${name}' type='hidden'/>
			<input id='${id}-datepicker' name='${name}-datepicker' type='text' class='date ${cssClass}' size="${size}" />
			<script type=\'text/javascript\'>

				jQuery(document).ready(function() {

					jQuery(".clear-date").click(function() {
						jQuery('#${id}-datepicker').val('');
					});

					jQuery('#${id}-datepicker').datepicker({
						altField: '#${id}',
						altFormat: 'mm/dd/yy',
						dateFormat: 'dd/M/yy',
						autoSize: ${autoSize},
						showOn: '${showOn}',
						changeMonth: ${changeMonthAndYear},
						changeYear: ${changeMonthAndYear},
						buttonImageOnly: true,
						buttonImage: '${request.contextPath}/images/icons/silk/calendar.png',
						//buttonText: '...',
						//showButtonPanel: true,
						//showOtherMonths: true,
						//selectOtherMonths: true
					});
					
					// If we reset the date, we need to reset the hidden form field as well.
					jQuery('#${id}-datepicker').change(function() { 
						if (this.value == '') { 
							jQuery('#${id}').val('');
						}
					});

					// Set the date value if one was provided
					var dateValue = '${value}';
					if (dateValue && dateValue != 'null') {
						jQuery('#${id}-datepicker').datepicker('setDate', new Date('${value}'));
					}
				});
			</script>
		</span>
		""";

		if (showTrigger) {
			html += """
			<style>
			.ui-datepicker-trigger {
				#position: relative; left: -16px; top: -1px;
			}
			</style>
			""";
		}
		
				
		out << html;
				
	}
	
	
}