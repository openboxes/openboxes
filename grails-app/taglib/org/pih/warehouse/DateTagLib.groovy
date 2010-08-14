package org.pih.warehouse

import java.text.SimpleDateFormat;

class DateTagLib {
   	
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
