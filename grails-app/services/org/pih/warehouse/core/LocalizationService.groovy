package org.pih.warehouse.core

import java.util.Locale;
import org.springframework.web.context.request.RequestContextHolder;

class LocalizationService {

	// TODO: do we need to make this read-only?
    boolean transactional = false

	// session-scoped (because it needs access to the user)
	static scope = "session"
	
	static final def delimiter = '\\|'
	static final def localeDelimiter = ':'
		
	/**
	 * Returns the value associated with the passed locale
	 */
	String getLocalizedString(String value) {
	
		// null check
		if (!value) {
			return value
		}
		
		// fetch the locale of the current user
		Locale locale = RequestContextHolder.currentRequestAttributes().getSession().user.locale
		
		// split into the the various localized values
		def values = value.split(delimiter)
		
		// if there aren't any values, return empty string
		if (values.size() == 0) {
			return "";
		}
		
		// the default value is the first value in the list
		def defaultValue = values[0]
		
		// if there is only one value, or if no locale has been specified, just return the default value
		if (values.size() == 1 || locale == null) {
			return defaultValue
		}
		
		// the other values are the potential localized values
		def localizedValues = values[1..values.size()-1]
		
		// see if we can find the user locale in the list of localized values
		def localizedValue
		
		localizedValues.each { 
			if (it.split(localeDelimiter).size() == 2) {   // sanity check that we have just two values (the locale code and the value)
				if (it.split(localeDelimiter)[0] == locale.getLanguage()) { 
					localizedValue = it.split(localeDelimiter)[1]
					return 
				}
			}
		}
		
		if (localizedValue) {
			// if we've found a localized value for the current locale
			return localizedValue
		}
		else {
			// otherwise, just return the default
			return defaultValue
		}
	}			
}
