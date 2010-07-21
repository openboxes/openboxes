package org.pih.warehouse

import java.text.SimpleDateFormat;

class DateTagLib {
   	
	def jqueryDatePicker = {attrs, body ->
		
		def name = attrs.name;
		
		def value = (attrs.format && attrs.value) ? new SimpleDateFormat(attrs.format).format(attrs.value) : ""

		if (name == null) { 
			throw new IllegalArgumentException("name parameter must be specified")			
		}
		
		out << "<script type=\'text/javascript\'> "
		out << "jQuery(function() { "
		out << "	jQuery('#" + name + "Widget').datepicker({ "
		out << "		showOn: 'both', "
		out << "		altField: '#" + name + "', "
		out << "		altFormat: 'mm/dd/yy', "
		out << "		dateFormat: 'dd M yy', "
		out << "		autoSize: true, "
		out << "		closeText: 'Done', "
		out << "		showButtonPanel: true, "
		out << "		showOtherMonths: true, "
		out << "		selectOtherMonths: true "
		out << "	}); "			
		out << "}); "
		out << "</script> "

		out << "<input id='" + name + "' name='" + name + "' value='" + value + "' type='hidden'/> "
		out << "<input id='" + name + "Widget' name='" + name + "Widget' value='" + value + "' type='text' class='date' width='8' /> "

	}
}
