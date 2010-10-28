package org.pih.warehouse

import java.text.SimpleDateFormat;
import java.util.Date;

class DateTagLib {
   	
	def relativeDate = { attrs, body ->
		
		Date now = new Date();
		Date date = attrs.date;
		
		if (date) { 
			def days = date - now;
			
			if (days == 0) { 
				out << "today";
			}
			else if (days > 0) { 
				out << "in ${days} days";
			} 
			else if (days < 0) { 
				out << "${-days} days ago"
			}
		}
	}
		
}
