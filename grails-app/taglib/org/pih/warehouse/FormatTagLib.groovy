package org.pih.warehouse

package org.pih.warehouse.util.LocalizationUtil

class FormatTagLib {
	
	static namespace = "format"
	
	Locale defaultLocale = new Locale(grailsApplication.config.warehouse.defaultLocale)
	
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
	 *  Formats a Time
	 *  @attr obj REQUIRED the date to format
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
  
  
	 /**
	  * Formats an Expiration Date
	  * @attr obj REQUIRED the date to format
	 */
	 def expirationDate = { attrs, body ->
		 if (attrs.obj != null) {
			 DateFormat df = new SimpleDateFormat(Constants.DEFAULT_MONTH_YEAR_DATE_FORMAT)
			 out << df.format(attrs.obj)
		 }
	 }

	 /**
	  * Custom tag to display a product
	  */
	 def product = { attrs ->		 
	 	if (attrs.product != null) {
			 // default format is to display the localized name of the product 
			 out << LocalizationUtil.getLocalizedString(attrs.product.name, locale: session?.user?.locale ?: defaultLocale)
		 }
		 // TODO: add more formats
	 }
	 
	 /**
	 * Custom tag to display a category
	 */
	def category = { attrs ->
		if (attrs.category != null) {
			// default format is to display the localized name of the catergory
			out << LocalizationUtil.getLocalizedString(attrs.product.name, locale: session?.user?.locale ?: defaultLocale)
		}
		// TODO: add more formats
	}
	 
	 /**
	  * Custom tag to display warehouse metadata
	  */
	 def metadata = { attrs ->
		 if (attrs.obj != null) {
			
			 // first, handle any Enums
			 if (attrs.obj instanceof Enum) {
				 // by convention, the localized text for a Enum is stored in the message property enum.className.value  (ie enum.ShipmentStatusCode.PENDING)
				 String className = attrs.obj.getClass().getSimpleName()		 
				 out << warehouse.message(code:'enum.' + className + "." + attrs.obj)
			 }
			 else {
				 // for all other objects, return the localized version of the name
				 out << LocalizationUtil.getLocalizedString(attrs.product.name, locale: session?.user?.locale ?: defaultLocale)
			 }
		 }
	 }
 }
