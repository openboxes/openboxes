package org.pih.warehouse

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ocpsoft.pretty.time.PrettyTime;

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
	
	def prettyDateFormat = { attrs, body ->
		def date = (attrs.date)?:new Date();
		def p = new PrettyTime();
		
		def now = new Date() 
		if (now - date < 1) { 
			out << "${warehouse.message(code:'default.today.label')}"
		}
		else { 
			out << 	p.format(date);
		}
	}
		
}
