package org.pih.warehouse

class DateTagLib {
   	
	def jqueryDatePicker = {attrs, body ->
		
		def name = attrs.name;
		
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

		out << "<input id='" + name + "' name='" + name + "' type='hidden'/> "
		out << "<input id='" + name + "Widget' name='" + name + "Widget' type='text' class='date' width='8' /> "

	}
}
