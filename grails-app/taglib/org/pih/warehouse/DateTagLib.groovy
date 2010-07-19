package org.pih.warehouse

class DateTagLib {
    /*
	def thisYear = {
    	out << Calendar.getInstance().get(Calendar.YEAR)
    }*/
	
	def jquery_datepicker = {attrs, body ->
		
		def name = attrs.name;
		
		if (name == null) { 
			throw new IllegalArgumentException("name parameter must be specified")			
		}
		
		out << "<script type=\'text/javascript\'> "
		out << "jQuery(function() { "
		out << "	jQuery('#" + name + "Widget').datepicker({ "
		out << "		showOn: 'both', "
		out << "		buttonImage: '../images/icons/silk/calendar.png', "
		out << "		buttonImageOnly: true, "
		out << "		altField: '#" + name + "', "
		out << "		altFormat: 'mm/dd/yy', "
		out << "		dateFormat: 'dd M yy', "
		out << "		autoSize: true, "
		out << "		closeText: 'Done', "
		out << "		buttonText: '...', "
		out << "		showButtonPanel: true, "
		out << "		showOtherMonths: true, "
		out << "		selectOtherMonths: true "
		out << "	}); "			
		out << "}); "
		out << "</script> "

		out << "<input id='expectedShippingDate' name='" + name + "' type='hidden'/> "
		out << "<input id='expectedShippingDatePicker' name='" + name + "Widget' type='text' class='date' width='8' /> "

	}
}
