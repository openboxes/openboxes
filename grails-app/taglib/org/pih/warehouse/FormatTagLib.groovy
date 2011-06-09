package org.pih.warehouse

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pih.warehouse.core.Constants;

import com.ocpsoft.pretty.time.PrettyTime;

class FormatTagLib {
	
	static namespace = "format"
	
	/**
	 * Formats a Date
	 * @attr obj REQUIRED the date to format
	*/
	def date = { attrs, body ->
		if (attrs.obj != null) {
			DateFormat df = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT)
			out << df.format(attrs.obj)
		}
	}
	
	/**
	* Formats a DateTime
	* @attr obj REQUIRED the date to format
   */
   def datetime = { attrs, body ->
	   if (attrs.obj != null) {
		   DateFormat df = new SimpleDateFormat(Constants.DEFAULT_DATE_TIME_FORMAT)
		   TimeZone tz = session.timezone
		   if (tz != null) {
			   df.setTimeZone(tz)
		   }
		   out << df.format(attrs.obj)
	   }
   }
   
   /**
   * Formats a Time
   * @attr obj REQUIRED the date to format
  */
  def time = { attrs, body ->
	  if (attrs.obj != null) {
		  DateFormat df = new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT)
		  TimeZone tz = session.timezone
		  if (tz != null) {
			  df.setTimeZone(tz)
		  }
		  out << df.format(attrs.obj)
	  }
  }
}
